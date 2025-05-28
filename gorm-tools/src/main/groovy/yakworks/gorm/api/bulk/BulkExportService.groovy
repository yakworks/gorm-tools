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
import gorm.tools.repository.RepoLookup
import gorm.tools.utils.ServiceLookup
import grails.core.GrailsApplication
import grails.gorm.transactions.ReadOnly
import yakworks.api.Result
import yakworks.api.problem.data.DataProblem
import yakworks.commons.lang.Validate
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.IncludesKey
import yakworks.meta.MetaMapList

//XXX tests for all of this in BulkExportServiceSpec
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

    static <D> BulkExportService<D> lookup(Class<D> entityClass){
        ServiceLookup.lookup(entityClass, BulkExportService<D>, "defaultBulkExportService")
    }

    /**
     * Creates a bulk export job and puts in hazel queue
     */
    SyncJobEntity queueExportJob(Map qParams, String sourceId) {
        SyncJobArgs args = setupSyncJobArgs(qParams, sourceId)
        return syncJobService.queueJob(args)
    }

    /**
     * Starts a bulk import job
     */
    SyncJobEntity startJob(Long jobId) {
        SyncJobEntity job = syncJobService.getJob(jobId)
        assert job.state == SyncJobState.Queued
        changeJobStatusToRunning(jobId)

        Map qParams = job.params

        SyncJobArgs syncJobArgs = buildSyncJobArgs(qParams, job.sourceId)
        syncJobArgs.jobId = jobId
        SyncJobContext jobContext = syncJobService.initContext(syncJobArgs, null)
        bulkExport(jobContext)

        return syncJobService.getJob(jobId)
    }


    protected SyncJobArgs setupSyncJobArgs(Map qParams, String sourceId){
        SyncJobArgs args = SyncJobArgs.withParams(qParams)

        //make sure they passed in q or qsearch. queryArgs will have been populated if they did
        if(!args.queryArgs) {
            throw DataProblem.of('error.query.qRequired').detail("q criteria required").toException()
        }
        //give it the bulkImport type
        args.jobType = 'bulk.export'
        //do includes, one of the keys is required. This is not used on queue, only for run.
        if(!qParams.includes && !qParams.includesKey) {
            throw DataProblem.ex("includes or includesKey are required params")
        }

        args.sourceId = sourceId
        args.params['entityClassName'] = getEntityClass().name
        return args
    }

    /**
     * wrap doBulkParallel and calls bulk
     *
     * @param dataList the list of data maps to create
     * @param jobContext the jobContext for the job
     * @return the id, just whats in jobContext
     */
    protected Long bulkExport(SyncJobContext jobContext) {

        //make sure it has a job here.
        Validate.notNull(jobContext.jobId)

        try {
            //jobContext.args.entityClass = getEntityClass()
            doBulkExport(jobContext)
        } catch (ex) {
            //ideally should not happen as the pattern here is that all exceptions should be handled in doBulkParallel
            jobContext.updateWithResult(problemHandler.handleUnexpected(ex))
        }
        finally {
            jobContext.finishJob()
        }

        return jobContext.jobId
    }

    protected GormRepo<D> getRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

    /**
     * Run bulk export job
     */

    protected void doBulkExport(SyncJobContext jobContext) {
        try {
            //paginate and fetch data list, update job results for each page of data.
            eachPage(jobContext.args.queryArgs) { List pageData ->
                //This closure is called for each page of data, this will be inside readonly TRX
                //create metamap list with includes
                MetaMapList entityMapList = metaMapService.createMetaMapList(pageData, jobContext.args.includes)
                //hydrate it now so we dont later get the "could not initialize proxy - no Session" when converting to json
                entityMapList.hydrate()
                Result result = Result.OK().payload(entityMapList as List)
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
    protected SyncJobArgs buildSyncJobArgs(Map qParams, String sourceId) {
        SyncJobArgs args = SyncJobArgs.withParams(qParams)
        if(!qParams.includes && !qParams.includesKey) {
            throw DataProblem.ex("includes or includesKey are required params")
        }
        List bulkIncludes = includesConfig.getIncludes(qParams, [IncludesKey.list, IncludesKey.get], getEntityClass())
        args.includes = bulkIncludes
        args.sourceId = sourceId
        //bulk export always runs async and parallel
        args.async = true
        //XXX @SUD where does this come into play? why true
        args.parallel = true

        //XXX do we really need this?
        args.entityClass = entityClass

        //bulkexport always saves data in a file
        //args.saveDataAsFile = true
        args.dataFormat = SyncJobArgs.DataFormat.Payload
        return args
    }

    /**
     * Changes job state to Running before starting bulk export job
     */
    protected void changeJobStatusToRunning(Serializable jobId) {
        syncJobService.updateJob([id:jobId, state: SyncJobState.Running])
    }

    /**
     * Instead of loading all the data for bulkexport, it paginates and loads one page at a time
     */
    //XXX @SUD add tests for these, not reason not to be adding unit tests

    protected void eachPage(QueryArgs queryArgs, Closure cl) {
        Pager paginator = Pager.of(max:500) //XXX @SUD why 10? changed to 100, lets make it configurable
        //count total records based on query args and build a paginator
        paginator.recordCount = getTotalCount(queryArgs)

        //XXX @SUD test should show it paging through, its not, same results are done over and over.
        // you were not casting offset and max, so it return the fist pge over and over again
        paginator.eachPage { page, max, offset ->
            runPageQuery(queryArgs, Pager.of(max: max, page: page), cl)
            getRepo().flushAndClear()
        }
    }

    @ReadOnly
    protected void runPageQuery(QueryArgs queryArgs, Pager pager, Closure cl) {
        List pageData = getRepo().query(queryArgs).pagedList(pager)
        cl.call(pageData)
    }

    @ReadOnly
    protected Integer getTotalCount(QueryArgs queryArgs) {
        return getRepo().query(queryArgs).count() as Integer
    }

}
