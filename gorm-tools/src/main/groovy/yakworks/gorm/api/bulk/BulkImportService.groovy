/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import java.time.Duration
import java.time.LocalDateTime

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.job.SyncJobState
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoLookup
import gorm.tools.utils.ServiceLookup
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemCodes
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.IncludesKey
import yakworks.gorm.api.support.DataMimeTypes
import yakworks.gorm.config.GormConfig
import yakworks.json.groovy.JsonEngine

/**
 * Helper for getting things setup for bulk calls
 *
 * @author Joshua Burnett (@basejump)
 */
@Slf4j
@CompileStatic
class BulkImportService<D> {

    @Autowired SyncJobService syncJobService

    @Autowired IncludesConfig includesConfig

    @Autowired ProblemHandler problemHandler

    @Autowired CsvToMapTransformer csvToMapTransformer

    @Autowired GormConfig gormConfig

    BulkImporter<D> bulkImporter

    Class<D> entityClass // the domain class this is for

    BulkImportService(Class<D> entityClass){
        this.entityClass = entityClass
    }

    static <D> BulkImportService<D> lookup(Class<D> entityClass){
        ServiceLookup.lookup(entityClass, BulkImportService<D>, "defaultBulkImportService")
    }

    BulkImporter<D> getBulkImporter(){
        if (!bulkImporter) {
            this.bulkImporter = BulkImporter.lookup(getEntityClass())
        }
        return bulkImporter
    }

    SyncJobEntity process(BulkImportJobParams jobParams, List<Map> dataList){
        if(gormConfig.legacyBulk){
            return bulkImportLegacy(jobParams, dataList)
        }
        else {
            return bulkImport(jobParams, dataList)
        }
    }

    /**
     * Creates a Syncjob and que it up
     */
    SyncJobEntity queueJob(BulkImportJobParams jobParams, List<Map> payloadBody) {
        //set the entityClassName
        jobParams.entityClassName = getEntityClass().name

        Map data = jobParams.asJobData()

        //if payloadId, then probably attachmentId with csv for example. Just store it and dont do payload conversion
        if(jobParams.attachmentId) {
            data.payloadId = jobParams.attachmentId
        }
        else if(payloadBody){
            if (jobParams.savePayloadAsFile || payloadBody.size() > 1000) {
                //we need to create the jobId so associate the attachment
                Long jobId = syncJobService.generateId()
                data.id = jobId
                data.payloadId = syncJobService.writePayloadFile(jobId, payloadBody)
            }
            else {
                String res = JsonEngine.toJson(payloadBody)
                data.payloadBytes = res.bytes
            }
        } else {
            throw DataProblemCodes.EmptyPayload.get().toException()
        }

        return syncJobService.queueJob(data)
    }

    /**
     * Starts a bulk import job
     */
    SyncJobEntity runJob(Long jobId) {
        assert jobId
        SyncJobEntity job = syncJobService.getJob(jobId)
        BulkImportJobParams jobParams = BulkImportJobParams.withParams(job.params)
        SyncJobContext jobContext = syncJobService.startJob(job, setupSyncJobArgs(jobParams))

        List<Map> payloadList
        if(jobParams.attachmentId) {
            //attachment will normally be a CSV as if they are doing json it can be passed into as the request body
            if(!jobParams.payloadFormat || jobParams.payloadFormat == DataMimeTypes.csv){
                //sets the datalist from the csv instead of body
                payloadList = transformCsvToBulkList(jobParams.asMap())
            } else if (jobParams.payloadFormat == DataMimeTypes.json) {
                //FIXME finish this to allow passing a json file
                throw new UnsupportedOperationException("json attachment not supported yet")
            }
        }
        else if(job.payloadId || job.payloadBytes) { //if no attachmentId was passed to params then get the payload
            payloadList = JsonEngine.parseJson(job.payloadToString(), List<Map>)
        }
        jobContext.args.payload(payloadList)
        jobContext.payloadSize = payloadList.size()

        getBulkImporter().bulkImport(payloadList, jobContext)

        return syncJobService.getJob(jobId)
    }

    @Deprecated
    protected SyncJobEntity bulkImportLegacy(BulkImportJobParams jobParams, List<Map> dataList){
        //if attachmentId then assume its a csv
        if(jobParams.attachmentId) {
            //sets the datalist from the csv instead of body
            //Transform csv here, so bulk processing remains same, regardless the incoming payload is csv or json
            dataList = transformCsvToBulkList(jobParams.asMap())
        } else {
            //XXX dirty ugly hack since we were not consistent and now need to do clean up
            // RNDC expects async to be false by default when its not CSV
            // remove this else hack once this is done https://github.com/9ci/cust-rndc-ext/issues/215
            if(jobParams.async == null) jobParams.async = false
        }
        //If dataList is empty then error right away.
        if(dataList == null || dataList.isEmpty())
            throw DataProblem.of('error.data.emptyPayload').detail("Bulk Data is Empty").toException()

        SyncJobArgs syncJobArgs = setupSyncJobArgs(jobParams)

        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, dataList)
        // This is the promise call. Will return immediately if syncJobArgs.async=true
        Long jobId = syncJobService.runJob(
            jobContext.args.asyncArgs, jobContext, () -> getBulkImporter().doBulkParallel(dataList, jobContext)
        )
        SyncJobEntity job = syncJobService.getJob(jobId)
        return job
    }

    //WIP
    protected SyncJobEntity bulkImport(BulkImportJobParams jobParams, List<Map> dataList){

        //submit the job
        SyncJobEntity job = queueJob(jobParams, dataList)
        Long jobId = job.id
        //if not async then wait for it to finish
        if(!jobParams.async){
            var startTime = LocalDateTime.now()
            //sleep for a second first to give the job runner time to pick it up
            sleep(1000)
            while(true){
                job = syncJobService.getJob(jobId)
                //not running and not queue
                if(job.state != SyncJobState.Running && job.state != SyncJobState.Queued){
                    break
                }
                long elapsedSeconds = Duration.between(startTime, LocalDateTime.now()).toSeconds()
                if(elapsedSeconds >= gormConfig.bulk.asyncTimeout.toSeconds()) {
                    throw DataProblem.of('error.timeout')
                        .detail(
                            "Job is still running but request timeout has occurred. Do not re-run. Check job status on $jobId for current state"
                        ).payload(jobId).toException()
                }
                else {
                    //sleep for a second for first 10 seconds.
                    //then sleep for 5 seconds until time out occurs.
                    long sleepTime = elapsedSeconds < 10 ? 1000 : 5000
                    sleep(sleepTime)
                }
            }
        }

        return job
    }


    /**
     * To be used for TESTING. Not meant for a production method.
     * queue and start the job
     */
    SyncJobEntity queueAndRun(BulkImportJobParams jobParams, List<Map> payloadBody) {
        SyncJobEntity jobEnt = queueJob(jobParams, payloadBody)
        return runJob(jobEnt.id)
    }

    List<Map> transformCsvToBulkList(Map gParams) {
        return getCsvToMapTransformer().process(gParams)
    }

    /**
     * sets up the SyncJobArgs from whats passed in from params.
     * NOTE: When queueing job much of this is not needed but we keep one common method
     */
    SyncJobArgs setupSyncJobArgs(BulkImportJobParams jobParams){
        List bulkIncludes = jobParams.includes ?: includesConfig.findByKeys(getEntityClass(), [IncludesKey.bulk, IncludesKey.get])
        //want the error includes to be blank if its not there
        List bulkErrorIncludes = includesConfig.getByKey(getEntityClass(), 'bulkError') as List<String>

        SyncJobArgs syncJobArgs = SyncJobArgs.withParams(jobParams.asMap())
        syncJobArgs.includes = bulkIncludes
        syncJobArgs.errorIncludes = bulkErrorIncludes
        //used for events?
        syncJobArgs.entityClass = getEntityClass()

        return syncJobArgs
    }

    GormRepo<D> getRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

}
