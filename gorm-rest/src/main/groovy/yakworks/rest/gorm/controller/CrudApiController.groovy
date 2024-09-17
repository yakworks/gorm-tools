/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.controller

import groovy.transform.CompileStatic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver
import org.springframework.http.HttpStatus

import gorm.tools.beans.Pager
import gorm.tools.job.JobUtils
import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.model.DataOp
import grails.web.Action
import yakworks.api.problem.Problem
import yakworks.etl.csv.CsvToMapTransformer
import yakworks.gorm.api.CrudApi
import yakworks.gorm.api.DefaultCrudApi
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.IncludesKey
import yakworks.gorm.api.IncludesProps

import static gorm.tools.problem.ProblemHandler.isBrokenPipe
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.MULTI_STATUS
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

/**
 * This is the CRUD controller for entities
 * @param <D> Object
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
@SuppressWarnings(['CatchRuntimeException'])
trait CrudApiController<D> extends RestApiController {

    static picklistMax = 50

    static allowedMethods = [
        post: "POST", put: ["PUT", "POST"], bulkUpdate: "POST", bulkCreate: "POST", delete: "DELETE"
    ] //patch: "PATCH",

    //Need it to access log and still compile static in trait (See https://issues.apache.org/jira/browse/GROOVY-7439)
    //final private static Logger log = LoggerFactory.getLogger(this.class)
    @SuppressWarnings(['LoggerWithWrongModifiers'])
    Logger log = LoggerFactory.getLogger(this.class)

    /** Not required but if an CrudApi bean is setup then it will get get used */
    @Autowired(required = false)
    CrudApi<D> crudApi

    @Autowired
    CsvToMapTransformer csvToMapTransformer

    /**
     * The java class for the Gorm domain (persistence entity). will generally get set in constructor or using the generic as
     * done in {@link gorm.tools.repository.GormRepo#getEntityClass}
     * using the {@link org.springframework.core.GenericTypeResolver}
     * @see org.grails.datastore.mapping.model.PersistentEntity#getJavaClass() .
     */
    Class<D> entityClass // the domain class this is for

    /**
     * The gorm domain class. uses the {@link org.springframework.core.GenericTypeResolver} is not set during contruction
     */
    Class<D> getEntityClass() {
        if (!entityClass) this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), CrudApiController)
        return entityClass
    }

    CrudApi<D> getCrudApi(){
        if (!crudApi) this.crudApi = DefaultCrudApi.of(getEntityClass())
        //crudApi.debugEnabled = log.isDebugEnabled()
        return crudApi
    }

    /**
     * GET /api/entity/${id}
     */
    @Action
    def get() {
        try {
            Map qParams = getParamsMap()
            Serializable idx = qParams.id as Serializable
            Map entityMap = getCrudApi().get(idx, qParams).asMap()
            respondWith(entityMap, [status: OK, params: qParams])
        } catch (Exception | AssertionError e) {
            handleThrowable(e)
        }
    }

    /**
     * POST /api/entity
     * Create with data
     */
    @Action
    def post() {
        try {
            Map qParams = getParamsMap()
            Map entityMap = getCrudApi().create(bodyAsMap(), qParams).asMap()
            respondWith(entityMap, [status: CREATED, params: qParams])
        } catch (Exception | AssertionError e) {
            handleThrowable(e)
        }
    }

    /**
     * PUT /api/entity/${id}
     * Update with data
     */
    @Action
    def put() {
        try {
            Map qParams = getParamsMap()
            Map entityMap = getCrudApi().update(bodyAsMap(), qParams).asMap()
            respondWith(entityMap, [status: OK, params: qParams])
        } catch (Exception | AssertionError e) {
            handleThrowable(e)
        }
    }

    /**
     * DELETE /api/entity/${id}
     */
    @Action
    def delete() {
        try {
            Map qParams = getParamsMap()
            getCrudApi().removeById((Serializable) qParams.id, qParams) //this should be fine since grails isnt loosing the params set from UrlMappings
            callRender(status: NO_CONTENT) //204
        } catch (Exception | AssertionError e) {
            handleThrowable(e)
        }
    }

    @Action
    def index() {
        list()
    }

    /**
     * request type is handled in urlMapping
     *
     * returns the list of domain objects
     */
    @Action
    def list() {
        try {
            Map qParams = getParamsMap()
            log.debug("list with gParams ${qParams}")
            Pager pager = getCrudApi().list(qParams, [IncludesKey.list.name()])
            //we pass in the params to args so it can get passed on to renderer, used in the excel renderer for example
            respondWith(pager, [params: qParams])
        } catch (Exception | AssertionError e) {
            handleThrowable(e)
        }
    }

    @Action
    def picklist() {
        try {
            Map qParams = getParamsMap()
            qParams.max = qParams.max ?: getPicklistMax() //default to 50 for picklists
            Pager pager = getCrudApi().list(qParams, ['picklist', IncludesKey.stamp.name()])
            respondWith(pager, [params: qParams])
        } catch (Exception | AssertionError e) {
            handleThrowable(e)
        }
    }

    /** Used for bulk create calls when Job object is returned */
    @Action
    def bulkCreate() {
        try {
            bulkProcess(DataOp.add)
        } catch (Exception | AssertionError e) {
            respondWith(
                BulkExceptionHandler.of(getEntityClass(), problemHandler).handleBulkOperationException(request, e)
            )
        }
    }

    /** Used for bulk create calls when Job object is returned */
    @Action
    def bulkUpdate() {
        try {
            bulkProcess(DataOp.update)
        } catch (Exception | AssertionError e) {
            respondWith(
                BulkExceptionHandler.of(getEntityClass(), problemHandler).handleBulkOperationException(request, e)
            )
        }
    }

    void bulkProcess(DataOp dataOp) {
        List dataList = bodyAsList() as List<Map>
        Map qParams = getParamsMap()

        String sourceId = JobUtils.requestToSourceId(request)

        //if attachmentId then assume its a csv
        if(qParams.attachmentId) {
            // We set savePayload to false by default for CSV since we already have the csv file as attachment?
            qParams.savePayload = false
            //sets the datalist from the csv instead of body
            dataList = transformCsvToBulkList(qParams)
        } else {
            //XXX dirty ugly hack since we were not consistent and now need to do clean up
            // RNDC expects async to be false by default when its not CSV
            if(!qParams.containsKey('async')) qParams['async'] = false
        }

        SyncJobEntity job = getCrudApi().bulk(dataOp, dataList, qParams, sourceId)
        respondWith(job, [status: MULTI_STATUS])
    }

    /**
     * transform csv to list of maps using csvToMapTransformer.
     * Override this to provide a different method.
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
    List<Map> transformCsvToBulkList(Map gParams) {
        return getCsvToMapTransformer().process(gParams)
    }

    /**
     * Helper method to convert entity instance to map and respond
     */
    void respondWithMap(D instance, Map mParams, HttpStatus status = HttpStatus.OK){
        Map entityMap = getCrudApi().entityToMap(instance, IncludesProps.of(mParams))
        respondWith(entityMap, [status: status, params: mParams])
    }

    @Override
    def handleException(Exception e) {
        /*
         * Broken pipe exception occurs when connection is closed before server has finished writing response.
         * Once that happens, trying to write any response to output stream will result in broken pipe.
         * We have "caught" broken pipe, and now during "catch" here, if we again try "respondWith" it will again result in "broken pipe" error
         */
        if (isBrokenPipe(e)) {
            return
        }
        else {
            Problem apiError = problemHandler.handleException(e, getEntityClass()?.simpleName)
            respondWith(apiError)
        }
    }

    void handleThrowable(Throwable e) {
        Problem apiError = problemHandler.handleException(e, getEntityClass()?.simpleName)
        respondWith(apiError)
    }
}