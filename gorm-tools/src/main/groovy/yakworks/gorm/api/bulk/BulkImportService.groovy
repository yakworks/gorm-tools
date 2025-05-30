/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

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
     * Creates a bulk import job and puts in hazel queue
     */
    SyncJobEntity queueJob(BulkImportJobParams jobParams, List<Map> payloadBody) {
        //set the entityClassName
        jobParams.entityClassName = getEntityClass().name
        SyncJobArgs args = setupSyncJobArgs(jobParams)

        //if attachmentId then assume its a csv
        if(jobParams.attachmentId) {
            args.payloadId = jobParams.attachmentId
            //XXX default payLoad format is CSV
        } else if(payloadBody){
            args.payload = payloadBody
        } else {
            throw DataProblemCodes.EmptyPayload.get().toException()
        }

        return syncJobService.queueJob(args)
    }

    /**
     * Starts a bulk import job
     */
    SyncJobEntity startJob(Long jobId) {
        SyncJobEntity job = syncJobService.getJob(jobId)
        assert job.state == SyncJobState.Queued
        syncJobService.changeJobStatusToRunning(jobId)

        List<Map> dataList
        BulkImportJobParams jobParams = BulkImportJobParams.withParams(job.params)
        //XXX right now we assume its csv in zip but we should have more info stored to say that.
        // Check the payloadFormat if its a zip to verify its CSV
        if(jobParams.attachmentId) {
            //sets the datalist from the csv instead of body
            dataList = transformCsvToBulkList(jobParams.asMap())
        }
        //if no attachmentId was passed to params then get the payload
        else if(job.payloadId || job.payloadBytes) {
            dataList = JsonEngine.parseJson(job.payloadToString(), List<Map>)
        }

        SyncJobArgs syncJobArgs = setupSyncJobArgs(jobParams)
        syncJobArgs.jobId = jobId

        SyncJobContext sctx = syncJobService.initContext(syncJobArgs, dataList)

        Long jobIdent = getBulkImporter().bulkImport(dataList, sctx)

        return syncJobService.getJob(jobIdent)
    }

    @Deprecated
    protected SyncJobEntity bulkImportLegacy(BulkImportJobParams jobParams, List<Map> dataList){
        //if attachmentId then assume its a csv
        //XXX we should not assume its CSV. Check dataFormat as well, we can have that default to CSV
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
        if(dataList == null || dataList.isEmpty()) throw DataProblem.of('error.data.emptyPayload').detail("Bulk Data is Empty").toException()

        SyncJobArgs syncJobArgs = setupSyncJobArgs(jobParams)
        syncJobArgs.jobType = 'bulk.import'
        syncJobArgs.entityClass = getEntityClass()

        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, dataList)
        //XXX why are we setting session: true here? explain. should it be default?
        //def asyncArgs = jobContext.args.asyncArgs.session(true)
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
            //sleep first to give the job runner time to pick it up
            sleep(1000)
            //XXX new process loop and wait for job to finish
            // should be on a timer loop coming from config.
            // normaly the http timeout will be 60-120 seconds, so lets start with 90 seconds and time out.
            while(true){
                job = syncJobService.getJob(jobId)
                //not running and not queue
                if(job.state != SyncJobState.Running && job.state != SyncJobState.Queued){
                    break
                }
                sleep(1000)
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
        return startJob(jobEnt.id)
    }

    /**
     * Changes job state to Running before starting bulk export job
     */
    //XXX @SUD incorporate this
    protected void changeJobStatusToRunning(Serializable jobId) {
        syncJobService.updateJob([id:jobId, state: SyncJobState.Running])
    }


    List<Map> transformCsvToBulkList(Map gParams) {
        return getCsvToMapTransformer().process(gParams)
    }

    /**
     * sets up the SyncJobArgs from whats passed in from params.
     * NOTE: When queing job much of this is not needed but we keep one common method
     */
    SyncJobArgs setupSyncJobArgs(BulkImportJobParams jobParams){
        List bulkIncludes = jobParams.includes ?: includesConfig.findByKeys(getEntityClass(), [IncludesKey.bulk, IncludesKey.get])
        //want the error includes to be blank if its not there
        List bulkErrorIncludes = includesConfig.getByKey(getEntityClass(), 'bulkError') as List<String>

        SyncJobArgs syncJobArgs = SyncJobArgs.withParams(jobParams.asMap())
        syncJobArgs.op = jobParams.op
        syncJobArgs.includes = bulkIncludes
        syncJobArgs.errorIncludes = bulkErrorIncludes
        syncJobArgs.sourceId = jobParams.sourceId
        //XXX do we need this for events?
        syncJobArgs.entityClass = getEntityClass()
        //give it the bulkImport type
        syncJobArgs.jobType = 'bulk.import'

        return syncJobArgs
    }

    GormRepo<D> getRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

}
