/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.support

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
    BulkExportService bulkExportService

    @Autowired
    CsvToMapTransformer csvToMapTransformer

    Class<D> entityClass // the domain class this is for

    BulkApiSupport(Class<D> entityClass){
        this.entityClass = entityClass
    }

    public static <D> BulkApiSupport<D> of(Class<D> entityClass){
        def bcs = new BulkApiSupport(entityClass)
        AppCtx.autowire(bcs)
        // bcs.syncJobService = AppCtx.get('syncJobService', SyncJobService)
        // if(AppCtx.ctx.containsBean('csvToMapTransformer')) bcs.csvToMapTransformer = AppCtx.get('csvToMapTransformer', CsvToMapTransformer)
        return bcs
    }

    SyncJobEntity submitJob(DataOp dataOp, Map qParams, String sourceId, List<Map> payloadBody) {
        SyncJobArgs args = setupSyncJobArgs(dataOp, qParams, sourceId)
        //add dataOp to params
        qParams['dataOp'] = dataOp.toString()
        Map data = [
            id: args.jobId,
            source: args.source,
            sourceId: args.sourceId,
            state: SyncJobState.Queued,
            params: qParams
        ] as Map<String,Object>

        //if attachmentId then assume its a csv
        if(qParams.attachmentId) {
            data.payloadId = qParams.attachmentId
        } else if(payloadBody){
            data.payload = payloadBody
        }

        SyncJobEntity jobEntity = syncJobService.repo.create(data, [flush: true, bindId: true]) as SyncJobEntity

        return jobEntity
    }

    SyncJobEntity startJob(Long jobId) {
        SyncJobEntity job = syncJobService.getJob(jobId)
        List<Map> dataList
        Map qParams = job.params
        //1. check payloadId and see if its a zip and csv
        //XXX right now we assume its csv in zip but we should have more info stored to say that.
        if(qParams.attachmentId) {
            //We set savePayload to false by default for CSV since we already have the csv file as attachment
            qParams.savePayload = false
            //sets the datalist from the csv instead of body
            dataList = transformCsvToBulkList(qParams)
        } else if(job.payloadId || job.payloadBytes) {
            dataList = JsonEngine.parseJson(job.payloadToString(), List<Map>)
        }
        //2. get dataOp from params
        DataOp dataOp = qParams['dataOp'] as DataOp
        assert(dataOp)

        SyncJobArgs syncJobArgs = setupSyncJobArgs(dataOp, qParams, job.sourceId)
        SyncJobContext sctx = syncJobService.initContext(syncJobArgs, dataList)
        //run it.
        getRepo().bulkProcess(dataList, sctx)

        return syncJobService.getJob(jobId)
    }


    List<Map> transformCsvToBulkList(Map gParams) {
        return getCsvToMapTransformer().process(gParams)
    }

    SyncJobEntity process(List<Map> dataList, SyncJobArgs syncJobArgs) {
        Long jobId = getRepo().bulk(dataList, syncJobArgs)
        SyncJobEntity job = syncJobService.getJob(jobId)
        return job
    }

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
        Long jobId = bulkExportService.scheduleBulkExportJob(args)
        return syncJobService.getJob(jobId)
    }


    GormRepo<D> getRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

}
