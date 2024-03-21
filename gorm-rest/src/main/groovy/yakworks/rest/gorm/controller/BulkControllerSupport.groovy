/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.controller

import javax.inject.Inject
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
import yakworks.etl.csv.CsvToMapTransformer
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
@SuppressWarnings(['CatchRuntimeException'])
class BulkControllerSupport<D> {

    @Autowired(required = false)
    SyncJobService syncJobService

    @Autowired(required = false)
    CsvToMapTransformer csvToMapTransformer

    @Autowired(required = false)
    IncludesConfig includesConfig

    @Inject ProblemHandler problemHandler

    Class<D> entityClass // the domain class this is for

    BulkControllerSupport(Class<D> entityClass){
        this.entityClass = entityClass
    }

    public static <D> BulkControllerSupport<D> of(Class<D> entityClass){
        def bcs = new BulkControllerSupport(entityClass)
        AppCtx.autowire(bcs)
        // bcs.syncJobService = AppCtx.get('syncJobService', SyncJobService)
        // if(AppCtx.ctx.containsBean('csvToMapTransformer')) bcs.csvToMapTransformer = AppCtx.get('csvToMapTransformer', CsvToMapTransformer)
        return bcs
    }

    SyncJobEntity process(List<Map> dataList, SyncJobArgs syncJobArgs) {
        //SyncJobArgs syncJobArgs = setupSyncJobArgs(dataOp, webRequest.params, webRequest.currentRequest)
        Long jobId
        if(syncJobArgs.params.attachmentId) {
            jobId = doBulkCsv(syncJobArgs)
        } else {
            //XXX dirty ugly hack since we were not consistent and now need to do clean up
            // RNDC expects async to be false by default when its not CSV
            syncJobArgs.async = syncJobArgs.params.getBoolean('async', false)

            jobId = getRepo().bulk(dataList, syncJobArgs)
        }

        SyncJobEntity job = syncJobService.getJob(jobId)
        return job
    }

    /**
     * Bulk CSV upload.
     *
     * Process flow to use attachment:
     * 1. Zip data.csv
     * 2. Call POST /api/upload?name=myZip.zip, take attachmentId from the result
     * 3. Call POST /api/rally/<domain>/bulk with query params:
     *  - attachmentId=<attachment-id>
     *  - dataFilename= -- pass in data.csv and detail.csv as default of parameter for file names
     *  - headerPathDelimiter -- default is '.', pass in '_' for underscore (this is path delimiter for header names, not csv delimiter)
     * @param syncJobArgs the syncJobArgs that is setup, important to have params on it
     * @return the jobId
     */
    Long doBulkCsv(SyncJobArgs syncJobArgs){
        //params will have already been set in syncJobArgs and for CSV we have different defaults
        Map params = syncJobArgs.params
        // We set savePayload to false by default for CSV since we already have the csv file as attachment?
        syncJobArgs.savePayload = params.getBoolean('savePayload', false)
        List<Map> dataList = transformCsvToBulkList(params)
        return getRepo().bulk(dataList, syncJobArgs)
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

    /**
     * transform csv to list of maps using csvToMapTransformer.
     * Override this to provide a different method.
     */
    List<Map> transformCsvToBulkList(Map params) {
        return csvToMapTransformer.process(params)
    }

    GormRepo<D> getRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

    /**
     * Special handler for bulk operations, so that we can log/highight every bulk error we send.
     * Its here, because we cant have more thn one exception handler for "Exception" in controller
     */
    Problem handleBulkOperationException(HttpServletRequest req, Throwable e) {
        Problem apiError = problemHandler.handleException(getEntityClass(), e)
        if (apiError.status.code == 500) {
            String requestInfo = "requestURI=[${req.requestURI}], method=[${req.method}], queryString=[${req.queryString}]"
            log.warn("⛔️ 👉 Bulk operation exception ⛔️ \n $requestInfo \n $apiError.cause?.message")
        }
        return apiError
    }
}
