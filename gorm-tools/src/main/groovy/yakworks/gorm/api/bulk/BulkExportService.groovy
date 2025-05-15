/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.beans.Pager
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.job.SyncJobState
import gorm.tools.mango.api.QueryArgs
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepo
import grails.core.GrailsApplication
import yakworks.api.Result
import yakworks.api.problem.data.DataProblem
import yakworks.commons.lang.NameUtils
import yakworks.commons.lang.Validate
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.IncludesKey
import yakworks.json.groovy.JsonEngine
import yakworks.meta.MetaMapList
import yakworks.spring.AppCtx

//XXX this whole thing should use new flow
@CompileStatic
@Slf4j
class BulkExportService<D> {

    @Autowired SyncJobService syncJobService
    @Autowired GrailsApplication grailsApplication
    @Autowired MetaMapService metaMapService
    @Autowired ProblemHandler problemHandler
    @Autowired IncludesConfig includesConfig

    @Autowired CsvToMapTransformer csvToMapTransformer

    Class<D> entityClass // the domain class this is for

    boolean legacyBulk = true

    BulkExportService(Class<D> entityClass){
        this.entityClass = entityClass
    }

    /**
     * Creates a bulk export job and puts in hazel queue
     */
    SyncJobEntity queueExportJob(Map qParams, String sourceId) {
        SyncJobArgs args = setupBulkExportArgs(qParams, sourceId)
        return syncJobService.queueJob(args)
    }


    SyncJobArgs setupBulkExportArgs(Map params, String sourceId){
        List bulkIncludes = includesConfig.findByKeys(getEntityClass(), [IncludesKey.list, IncludesKey.get])
        SyncJobArgs syncJobArgs = SyncJobArgs.withParams(params)
        //syncJobArgs.op = DataOp.update.export
        syncJobArgs.includes = bulkIncludes
        syncJobArgs.sourceId = sourceId
        syncJobArgs.entityClass = getEntityClass()
        return syncJobArgs
    }

    SyncJobEntity processBulkExport(Map params, String sourceId) {
        SyncJobArgs args = setupBulkExportArgs(params, sourceId)
        Long jobId = scheduleBulkExportJob(args)
        return syncJobService.getJob(jobId)
    }



    /**
     * Creates a new bulk export job with status : "Queued" and QueryArgs stored in job payload
     */
    Long scheduleBulkExportJob(SyncJobArgs syncJobArgs) {
        if(syncJobArgs.queryArgs == null) throw DataProblem.of('error.query.qRequired').detail("q criteria required").toException()

        //Store QueryArgs and includes list as payload, these are the two things we need when running export
        //so that we can construct it back when syncjob runs
        Map payload = [
            q: syncJobArgs.queryArgs.criteriaMap,
            includes: syncJobArgs.includes,
            domain: syncJobArgs.entityClass.simpleName
        ]
        SyncJobEntity job = syncJobService.queueJob(syncJobArgs)
        return job.id
    }

    /**
     * Loads queued bulkexport job, recreates JobArgs and context from payload and runs the job
     *
     * @param async, default=true, can be passed false for tests to keep tests clean
     */
    Long runBulkExportJob(Long jobId, boolean async = true) {
        //build job context and syncjobargs from previously saved syncjob's payload
        SyncJobContext jobContext = buildJobContext(jobId)
        jobContext.args.async = async

        try {
            //change job state to running
            changeJobStatusToRunning(jobId)
            //load repo for the domain class name stored in payload
            GormRepo repo = loadRepo(jobContext.payload['domain'] as String)
            jobContext.args.entityClass = repo.entityClass
            doBulkExport(jobContext, repo)
        } catch (ex){
            //ideally should not happen as the pattern here is that all exceptions should be handled in doBulkParallel
            jobContext.updateWithResult(problemHandler.handleUnexpected(ex))
        }
        finally {
            jobContext.finishJob()
        }

        return jobId
    }

    /**
     * Run bulk export job
     */
    void doBulkExport(SyncJobContext jobContext, GormRepo repo) {
        try {
            //paginate and fetch data list, update job results for each page of data.
            eachPage(repo, jobContext.args.queryArgs) { List pageData ->
                //create metamap list with includes
                MetaMapList entityMapList = metaMapService.createMetaMapList(pageData, jobContext.args.includes)
                Result result = Result.OK().payload([data:entityMapList])
                //update job with page data
                jobContext.updateJobResults(result, false)
            }
        } catch (Exception ex) {
            log.error("BulkExport unexpected exception", ex)
            jobContext.updateWithResult(problemHandler.handleUnexpected(ex))
        }
    }


    /**
     * Recreates SyncjobArgs from previously slaved Syncjob.
     * Builds QueryArgs and includes from job payload
     */
    SyncJobArgs buildSyncJobArgs(Map payload) {
        Validate.notEmpty(payload, "job payload is empty")

        Map q = payload['q']
        List<String> includes = payload['includes']

        SyncJobArgs syncJobArgs = SyncJobArgs.withParams(q:q)
        syncJobArgs.includes = includes

        //bulk export always runs async and parallel
        syncJobArgs.async = true
        syncJobArgs.parallel = true

        //bulkexport always saves data in a file
        syncJobArgs.saveDataAsFile = true
        syncJobArgs.dataFormat = SyncJobArgs.DataFormat.Payload
        return syncJobArgs
    }

    /**
     * Recreate jobcontext and jobargs from saved job
     */
    SyncJobContext buildJobContext(Long jobId) {
        SyncJobEntity job = syncJobService.getJob(jobId)

        String payloadStr = job.payloadToString()
        Validate.notEmpty(payloadStr, "job payload is empty")
        Map payload =  JsonEngine.parseJson(payloadStr) as Map

        SyncJobContext context = SyncJobContext.of(buildSyncJobArgs(payload)).syncJobService(syncJobService).payload(payload)
        context.args.jobId = jobId
        return context
    }

    /**
     * Changes job state to Running before starting bulk export job
     */
    void changeJobStatusToRunning(Serializable jobId) {
        syncJobService.updateJob([id:jobId, state: SyncJobState.Running])
    }

    //XXX Why make it so complicated? We do repo look up all over and its much cleaner than this.
    // Whats wrong with keeping this centralized in RepoLookup
    GormRepo loadRepo(String domainName) {
        return AppCtx.get("${NameUtils.getPropertyName(domainName)}Repo") as GormRepo
    }

    /**
     * Instead of loading all the data for bulkexport, it paginates and loads one page at a time
     */
    void eachPage(GormRepo repo, QueryArgs queryArgs, Closure cl) {
        //count total records based on query args and build a paginator
        Integer totalRecords = repo.query(queryArgs).count() as Integer
        Pager paginator = Pager.of(max:10)
        paginator.recordCount = totalRecords

        paginator.eachPage { def max, def offset ->
            List pageData = repo.query(queryArgs).pagedList(Pager.of(max:max, offset:offset))
            cl.call(pageData)
        }
    }

}
