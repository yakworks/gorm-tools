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
import gorm.tools.repository.bulk.BulkExportService
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
