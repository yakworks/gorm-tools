/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.support

import javax.servlet.http.HttpServletRequest

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
import yakworks.api.problem.Problem
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.IncludesKey
import yakworks.spring.AppCtx

/**
 * This is the CRUD controller for entities
 * @param <D> Object
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@Slf4j
@CompileStatic
class BulkSupport<D> {

    @Autowired
    SyncJobService syncJobService

    @Autowired
    IncludesConfig includesConfig

    @Autowired
    ProblemHandler problemHandler

    Class<D> entityClass // the domain class this is for

    BulkSupport(Class<D> entityClass){
        this.entityClass = entityClass
    }

    public static <D> BulkSupport<D> of(Class<D> entityClass){
        def bcs = new BulkSupport(entityClass)
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
        Map includesMap = includesConfig.getIncludes(entityClass)
        List bulkIncludes = IncludesConfig.getFieldIncludes(includesMap, [IncludesKey.bulk.name()])
        List bulkErrorIncludes = includesMap['bulkError'] as List<String>

        SyncJobArgs syncJobArgs = SyncJobArgs.withParams(params)
        syncJobArgs.op = dataOp
        syncJobArgs.includes = bulkIncludes
        syncJobArgs.errorIncludes = bulkErrorIncludes
        syncJobArgs.sourceId = sourceId

        return syncJobArgs
    }

    GormRepo<D> getRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

}
