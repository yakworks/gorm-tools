/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.bulk

import javax.inject.Inject

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.job.SyncJobState
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepo
import grails.core.GrailsApplication
import yakworks.api.Result
import yakworks.api.problem.data.DataProblem
import yakworks.commons.lang.NameUtils
import yakworks.commons.lang.Validate
import yakworks.json.groovy.JsonEngine
import yakworks.meta.MetaMapList
import yakworks.spring.AppCtx

@CompileStatic
@Slf4j
class BulkExportService {

    @Inject SyncJobService syncJobService
    @Inject GrailsApplication grailsApplication
    @Inject MetaMapService metaMapService
    @Inject ProblemHandler problemHandler

    Long scheduleBulkExportJob(SyncJobArgs syncJobArgs) {
        if(syncJobArgs.queryArgs == null) throw DataProblem.of('error.query.qRequired').detail("q criteria required").toException()
        //resulting data should be saved as a file
        syncJobArgs.saveDataAsFile = true

        //Store QueryArgs and includes list as payload, these are the two things we need when running export
        //so that we can construct it back when syncjob runs
        Map payload = [
            q: syncJobArgs.queryArgs.criteriaMap,
            includes: syncJobArgs.includes,
            domain: syncJobArgs.entityClass.simpleName
        ]
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, payload)
        return jobContext.jobId
    }

    Long runBulkExportJob(Long jobId) {
        //build job context and syncjobargs from previously saved syncjob's payload
        SyncJobContext context = buildJobContext(jobId)

        //change job state to running
        changeJobStatusToRunning(jobId)

        //load repo for the domain
        GormRepo repo = loadRepo(context.payload['domain'] as String)
        context.args.entityClass = repo.entityClass

        //run job
        return syncJobService.runJob(context.args.asyncArgs, context, () -> doBulkExport(context, repo))
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

        return syncJobArgs
    }

    SyncJobContext buildJobContext(Long jobId) {
        SyncJobEntity job = syncJobService.getJob(jobId)

        String payloadStr = job.payloadToString()
        Validate.notEmpty(payloadStr, "job payload is empty")
        Map payload =  JsonEngine.parseJson(payloadStr) as Map

        return SyncJobContext.of(buildSyncJobArgs(payload)).syncJobService(syncJobService).payload(payload)
    }

    /**
     * Changes job state to Running before starting bulk export job
     */
    void changeJobStatusToRunning(Serializable jobId) {
        syncJobService.updateJob([id:jobId, state: SyncJobState.Running])
    }

    void doBulkExport(SyncJobContext jobContext, GormRepo repo) {
        try {
            //fetch data list
            List resultList = repo.query(jobContext.args.queryArgs).list()

            //create metamap list with includes
            MetaMapList entityMapList = metaMapService.createMetaMapList(resultList, jobContext.args.includes)

            //update job with result
            Result result = Result.OK().payload(entityMapList)
            jobContext.updateJobResults(result, false)
        } catch (Exception ex) {
            log.error("BulkExport unexpected exception", ex)
            jobContext.updateWithResult(problemHandler.handleUnexpected(ex))
        }
    }

    GormRepo loadRepo(String domainName) {
        return AppCtx.get("${NameUtils.getPropertyName(domainName)}Repo") as GormRepo
    }

}
