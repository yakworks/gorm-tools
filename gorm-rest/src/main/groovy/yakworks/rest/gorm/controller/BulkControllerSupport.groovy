/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.controller

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.DataOp
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
@CompileStatic
@SuppressWarnings(['CatchRuntimeException'])
class BulkControllerSupport<D> {

    @Autowired(required = false)
    SyncJobService syncJobService

    @Autowired(required = false)
    CsvToMapTransformer csvToMapTransformer

    @Autowired(required = false)
    IncludesConfig includesConfig

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
        //default to true for CSV unless explicitely disabled in params
        syncJobArgs.async = params.getBoolean('async', true)
        // dont save payload by default, but if its true then save to file not db.
        // TODO above comment is not true, its setting saveDataAsFile not pload
        syncJobArgs.savePayload = params.getBoolean('savePayload', false)
        //always save the data as file and not in the syncJob row
        //TODO why do we do this? why not let it be automated like elsewhere?
        syncJobArgs.saveDataAsFile = params.getBoolean('saveDataAsFile', true)

        List<Map> dataList = transformCsvToBulkList(params)
        return getRepo().bulk(dataList, syncJobArgs)
    }

    /**
     * sets up the SyncJobArgs from whats passed in from params
     */
    SyncJobArgs setupSyncJobArgs(DataOp dataOp, Map params, String sourceId){
        // HttpServletRequest req = webRequest.currentRequest
        // GrailsParameterMap params = webRequest.params
        // String sourceKey = "${req.method} ${req.requestURI}?${req.queryString}"

        Map includesMap = includesConfig.getIncludes(entityClass)
        List bulkIncludes = IncludesConfig.getFieldIncludes(includesMap, [IncludesKey.bulk.name()])
        List bulkErrorIncludes = includesMap['bulkError'] as List<String>

        SyncJobArgs syncJobArgs = new SyncJobArgs(
            op: dataOp,
            includes: bulkIncludes,
            errorIncludes: bulkErrorIncludes,
            sourceId: sourceId,
            source: params.jobSource,
            params: params
        )

        if(params.parallel != null) syncJobArgs.parallel = params.getBoolean('parallel')
        //async is false by default, when this is true then runs "non-blocking" in background and will job immediately with state=running
        if(params.async != null) syncJobArgs.async = params.getBoolean('async')
        //savePayload is true by default
        if(params.savePayload != null) syncJobArgs.savePayload = params.getBoolean('savePayload')
        //data is always saved, but can force it be in a file if passes. will get set to true if payload.size() > 1000 no matter what is set
        // syncJobArgs.saveDataAsFile = params.boolean('saveDataAsFile')
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

}
