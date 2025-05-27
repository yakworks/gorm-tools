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
import gorm.tools.repository.model.DataOp
import gorm.tools.utils.ServiceLookup
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemCodes
import yakworks.commons.lang.EnumUtils
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

    @Autowired
    SyncJobService syncJobService

    @Autowired
    IncludesConfig includesConfig

    @Autowired
    ProblemHandler problemHandler

    @Autowired
    CsvToMapTransformer csvToMapTransformer

    @Autowired
    GormConfig gormConfig

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


    SyncJobEntity process(DataOp dataOp, List<Map> dataList, Map qParams, String sourceId){
        if(gormConfig.legacyBulk){
            return bulkImportLegacy(dataOp, dataList, qParams, sourceId)
        } else {
            return doBulkImport(dataOp, dataList, qParams, sourceId)
        }
    }

    SyncJobEntity bulkImportLegacy(DataOp dataOp, List<Map> dataList, Map qParams, String sourceId){
        //if attachmentId then assume its a csv
        //XXX we should not assume its CSV. Check dataFormat as well, we can have that default to CSV
        if(qParams.attachmentId) {
            //XXX We set savePayload to false by default for CSV since we already have the csv file as attachment?
            qParams.savePayload = false
            //sets the datalist from the csv instead of body
            //Transform csv here, so bulk processing remains same, regardless the incoming payload is csv or json
            dataList = transformCsvToBulkList(qParams)
        } else {
            //XXX dirty ugly hack since we were not consistent and now need to do clean up
            // RNDC expects async to be false by default when its not CSV
            if(!qParams.containsKey('async')) qParams['async'] = false
        }
        SyncJobArgs syncJobArgs = setupSyncJobArgs(dataOp, qParams, sourceId)
        syncJobArgs.jobType = 'bulkImport'
        Long jobId = bulkLegacy(dataList, syncJobArgs)
        SyncJobEntity job = syncJobService.getJob(jobId)
        return job
    }

    /**
     * creates a supplier to wrap doBulkParallel and calls bulk
     * if syncJobArgs.async = true will return right away
     *
     * @param dataList the list of data maps to create
     * @param syncJobArgs the args object to pass on to doBulk
     * @return Job id
     */
    Long bulkLegacy(List<Map> dataList, SyncJobArgs syncJobArgs) {
        //If dataList is empty then error right away.
        if(dataList == null || dataList.isEmpty()) throw DataProblem.of('error.data.emptyPayload').detail("Bulk Data is Empty").toException()

        syncJobArgs.entityClass = getEntityClass()
        if(!syncJobArgs.jobType) syncJobArgs.jobType = 'bulk.import'

        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, dataList)
        //XXX why are we setting session: true here? explain. should it be default?
        //def asyncArgs = jobContext.args.asyncArgs.session(true)
        // This is the promise call. Will return immediately if syncJobArgs.async=true
        return syncJobService.runJob(
            jobContext.args.asyncArgs, jobContext, () -> getBulkImporter().doBulkParallel(dataList, jobContext)
        )
    }

    //WIP
    SyncJobEntity doBulkImport(DataOp dataOp, List<Map> dataList, Map qParams, String sourceId){

        //submit the job
        SyncJobEntity job = queueImportJob(dataOp, qParams, sourceId, dataList)
        Long jobId = job.id
        //if not async then wait for it to finish
        if(!qParams.getBoolean('async', true)){
            //sleep first to give the job runner time to pick it up
            sleep(1000)
            //XXX new process loop and wait for job to finish
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
     * Creates a bulk import job and puts in hazel queue
     */
    SyncJobEntity queueImportJob(DataOp dataOp, Map qParams, String sourceId, List<Map> payloadBody) {

        SyncJobArgs args = setupSyncJobArgs(dataOp, qParams, sourceId)
        //give it the bulkImport type
        args.jobType = 'bulk.import'
        //add dataOp to params if exists
        args.params['dataOp'] = dataOp.name()
        //make sure className is set to params
        args.params['entityClassName'] = getEntityClass().name

        //if attachmentId then assume its a csv
        //XXX add check for dataFormat
        if(qParams.attachmentId) {
            args.payloadId = qParams.attachmentId as Long
        } else if(payloadBody){
            args.payload = payloadBody
        } else {
            throw DataProblemCodes.EmptyPayload.get().toException()
        }

        return syncJobService.queueJob(args)
    }

    /**
     * Creates a bulk import job and puts in hazel queue
     */
    SyncJobEntity queueImportJob(SyncJobArgs args) {
        args.jobType = 'bulk.import'
        args.params['entityClassName'] = getEntityClass().name
        return syncJobService.queueJob(args)
    }


    /**
     * Starts a bulk import job
     */
    SyncJobEntity startJob(Long jobId) {
        SyncJobEntity job = syncJobService.getJob(jobId)
        assert job.state == SyncJobState.Queued

        List<Map> dataList
        Map qParams = job.params
        //1. check payloadId and see if its a zip and csv
        //XXX right now we assume its csv in zip but we should have more info stored to say that.
        // Check the dataFormat if its a zip to verify its CSV
        if(qParams.attachmentId) {
            //We set savePayload to false by default for CSV since we already have the csv file as attachment
            qParams.savePayload = false
            //sets the datalist from the csv instead of body
            dataList = transformCsvToBulkList(qParams)
        }
        else if(job.payloadId || job.payloadBytes) {
            //if no attachmentId was passed to params then
            dataList = JsonEngine.parseJson(job.payloadToString(), List<Map>)
        }
        //2. get dataOp from params
        DataOp dataOp = EnumUtils.getEnumIgnoreCase(DataOp, qParams['dataOp'] as String)

        SyncJobArgs syncJobArgs = setupSyncJobArgs(dataOp, qParams, job.sourceId)
        syncJobArgs.jobId = jobId

        SyncJobContext sctx = syncJobService.initContext(syncJobArgs, dataList)

        Long jobIdent = getBulkImporter().bulkImport(dataList, sctx)

        return syncJobService.getJob(jobIdent)
    }

    /**
     * To be used for TESTING. Not meant for a production method.
     * queue and start the job
     */
    SyncJobEntity queueAndRun(DataOp dataOp, Map qParams, String sourceId, List<Map> payloadBody){
        SyncJobEntity jobEnt = queueImportJob(dataOp, qParams, sourceId, payloadBody)
        return startJob(jobEnt.id)
    }

    /**
     * Changes job state to Running before starting bulk export job
     */
    //XXX @SUD incorporate this
    void changeJobStatusToRunning(Serializable jobId) {
        syncJobService.updateJob([id:jobId, state: SyncJobState.Running])
    }


    List<Map> transformCsvToBulkList(Map gParams) {
        return getCsvToMapTransformer().process(gParams)
    }

    /**
     * sets up the SyncJobArgs from whats passed in from params
     */
    SyncJobArgs setupSyncJobArgs(DataOp dataOp, Map params, String sourceId){
        List bulkIncludes = params.includes ? (List)params.includes : includesConfig.findByKeys(getEntityClass(), [IncludesKey.bulk, IncludesKey.get])
        //want the error includes to be blank if its not there
        List bulkErrorIncludes = includesConfig.getByKey(getEntityClass(), 'bulkError') as List<String>

        SyncJobArgs syncJobArgs = SyncJobArgs.withParams(params)
        syncJobArgs.op = dataOp
        syncJobArgs.includes = bulkIncludes
        syncJobArgs.errorIncludes = bulkErrorIncludes
        syncJobArgs.sourceId = sourceId
        //XXX do we need this for events?
        syncJobArgs.entityClass = getEntityClass()

        // for upsert they can pass in op=upsert to params.
        // This is different than the dataOp arg in method here, which is going to either be add or update already
        // as its set because it either a POST or PUT call.
        DataOp paramsOp = EnumUtils.getEnumIgnoreCase(DataOp, params.op as String)
        if(paramsOp == DataOp.upsert) syncJobArgs.op = paramsOp

        return syncJobArgs
    }

    GormRepo<D> getRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

}
