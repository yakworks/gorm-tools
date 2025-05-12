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
import yakworks.commons.lang.EnumUtils
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.IncludesKey
import yakworks.gorm.config.GormConfig
import yakworks.json.groovy.JsonEngine
import yakworks.spring.AppCtx

/**
 * Helper for getting things setup for bulk calls
 *
 * @author Joshua Burnett (@basejump)
 */
@Slf4j
@CompileStatic
class BulkApiSupport<D> {

    @Autowired
    SyncJobService syncJobService

    @Autowired
    IncludesConfig includesConfig

    @Autowired
    ProblemHandler problemHandler

    @Autowired
    CsvToMapTransformer csvToMapTransformer

    @Autowired GormConfig gormConfig

    Class<D> entityClass // the domain class this is for

    BulkApiSupport(Class<D> entityClass){
        this.entityClass = entityClass
    }

    static <D> BulkApiSupport<D> of(Class<D> entityClass){
        def bcs = new BulkApiSupport(entityClass)
        AppCtx.autowire(bcs)
        return bcs
    }

    SyncJobEntity bulkImport(DataOp dataOp, List<Map> dataList, Map qParams, String sourceId){
        if(gormConfig.legacyBulk){
            return bulkImportLegacy(dataOp, dataList, qParams, sourceId)
        } else {
            return doBulkImport(dataOp, dataList, qParams, sourceId)
        }
    }

    SyncJobEntity bulkImportLegacy(DataOp dataOp, List<Map> dataList, Map qParams, String sourceId){
        //if attachmentId then assume its a csv
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
        Long jobId = getRepo().bulk(dataList, syncJobArgs)
        SyncJobEntity job = syncJobService.getJob(jobId)
        return job
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
        //add dataOp to params if exists
        args.params['dataOp'] = dataOp.name()
        //if attachmentId then assume its a csv
        if(qParams.attachmentId) {
            args.payloadId = qParams.attachmentId as Long
        } else if(payloadBody){
            args.payload = payloadBody
        }

        return syncJobService.queueJob(args)
    }


    //WIP
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
        SyncJobContext sctx = syncJobService.initContext(syncJobArgs, dataList)

        //run it based on whether its import or export
        //bulk(dataList, sctx)

        return syncJobService.getJob(jobId)
    }


    List<Map> transformCsvToBulkList(Map gParams) {
        return getCsvToMapTransformer().process(gParams)
    }
    //
    // SyncJobEntity process(List<Map> dataList, SyncJobArgs syncJobArgs) {
    //     Long jobId = getRepo().bulk(dataList, syncJobArgs)
    //     SyncJobEntity job = syncJobService.getJob(jobId)
    //     return job
    // }

    /**
     * sets up the SyncJobArgs from whats passed in from params
     */
    SyncJobArgs setupSyncJobArgs(DataOp dataOp, Map params, String sourceId){
        List bulkIncludes = includesConfig.findByKeys(getEntityClass(), [IncludesKey.bulk, IncludesKey.get])
        //want the error includes to be blank if its not there
        List bulkErrorIncludes = includesConfig.getByKey(getEntityClass(), 'bulkError') as List<String>

        SyncJobArgs syncJobArgs = SyncJobArgs.withParams(params)
        syncJobArgs.op = dataOp
        syncJobArgs.includes = bulkIncludes
        syncJobArgs.errorIncludes = bulkErrorIncludes
        syncJobArgs.sourceId = sourceId

        //for upsert they can pass in op=upsert to params.
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
