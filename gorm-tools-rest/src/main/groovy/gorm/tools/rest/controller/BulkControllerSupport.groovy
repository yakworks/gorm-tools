/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic

import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.api.IncludesConfig
import gorm.tools.api.IncludesKey
import gorm.tools.beans.AppCtx
import gorm.tools.csv.CsvToMapTransformer
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.DataOp
import grails.util.TypeConvertingMap
import grails.web.servlet.mvc.GrailsParameterMap

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

    SyncJobEntity process(DataOp dataOp, List<Map> dataList, GrailsWebRequest webRequest) {
        // List dataList = bodyAsList() as List<Map>
        HttpServletRequest req = webRequest.currentRequest
        GrailsParameterMap params = webRequest.params
        String sourceKey = "${req.method} ${req.requestURI}?${req.queryString}"

        Map includesMap = includesConfig.getIncludes(entityClass)
        List bulkIncludes = IncludesConfig.getFieldIncludes(includesMap, [IncludesKey.bulk.name()])
        List bulkErrorIncludes = IncludesConfig.getFieldIncludes(includesMap, [IncludesKey.bulkError.name()])

        SyncJobArgs syncJobArgs = new SyncJobArgs(op: dataOp, includes: bulkIncludes, errorIncludes: bulkErrorIncludes,
            sourceId: sourceKey, source: params.jobSource, params: params)
        //Can override payload storage or turn off with 'NONE' if not needed for big loads
        syncJobArgs.promiseEnabled = params.boolean('promiseEnabled', false)
        syncJobArgs.savePayload = params.boolean('savePayload', true)
        syncJobArgs.saveDataAsFile = params.boolean('saveDataAsFile')

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
        if(!syncJobArgs.asyncEnabled == null) syncJobArgs.asyncEnabled  = true //enable by default
        //params will have already been set in syncJobArgs and for CSV we have different defaults
        TypeConvertingMap params = syncJobArgs.params as TypeConvertingMap
        //default to true for CSV unless explicitely disabled in params
        syncJobArgs.promiseEnabled = params.boolean('promiseEnabled', true)
        //dont save payload by default, and if done, save to file not db.
        syncJobArgs.savePayload = params.boolean('savePayload', false)
        syncJobArgs.saveDataAsFile = params.boolean('saveDataAsFile', true)

        List<Map> dataList = transformCsvToBulkList(params)
        return getRepo().bulk(dataList, syncJobArgs)
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
