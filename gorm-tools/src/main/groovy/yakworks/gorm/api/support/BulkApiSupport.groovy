/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.support

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.DataOp
import yakworks.commons.lang.EnumUtils
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.IncludesKey
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

    SyncJobEntity submitJob(DataOp dataOp, Map qParams, String sourceId, List<Map> dataList) {
        SyncJobArgs args = setupSyncJobArgs(dataOp, qParams, sourceId)
        Map data = [
            id: args.jobId, source: args.source, sourceId: args.sourceId,
            state: args.jobState
        ] as Map<String,Object>
            
        //if attachmentId then assume its a csv
        if(qParams.attachmentId) {
            // We set savePayload to false by default for CSV since we already have the csv file as attachment
            qParams.savePayload = false

        }


        // Map data = [
        //     id: args.jobId, source: args.source, sourceId: args.sourceId,
        //     state: args.jobState, payload: payload
        // ] as Map<String,Object>
        //
        // def jobEntity = syncJobService.repo.create(data, [flush: true, bindId: true]) as SyncJobEntity

        return null
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
