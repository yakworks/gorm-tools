/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.controller

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.InvokerHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver
import org.springframework.http.HttpStatus

import gorm.tools.beans.Pager
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.DataOp
import grails.web.Action
import yakworks.api.problem.Problem
import yakworks.gorm.api.IncludesKey

import static gorm.tools.problem.ProblemHandler.isBrokenPipe
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.MULTI_STATUS
import static org.springframework.http.HttpStatus.NO_CONTENT

/**
 * This is the CRUD controller for entities
 * @param <D> Object
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
@SuppressWarnings(['CatchRuntimeException'])
trait RestRepoApiController<D> extends RestApiController {

    static picklistMax = 50

    static allowedMethods = [
        post: "POST", put: ["PUT", "POST"], bulkUpdate: "POST", bulkCreate: "POST", delete: "DELETE"
    ] //patch: "PATCH",

    //Need it to access log and still compile static in trait (See https://issues.apache.org/jira/browse/GROOVY-7439)
    //final private static Logger log = LoggerFactory.getLogger(this.class)
    @SuppressWarnings(['LoggerWithWrongModifiers'])
    Logger log = LoggerFactory.getLogger(this.class)

    @Autowired(required = false)
    EntityResponder<D> entityResponder

    @Autowired(required = false)
    BulkControllerSupport<D> bulkControllerSupport

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
        if (!entityClass) this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), RestRepoApiController)
        return entityClass
    }

    EntityResponder<D> getEntityResponder(){
        if (!entityResponder) this.entityResponder = EntityResponder.of(getEntityClass())
        entityResponder.debugEnabled = log.isDebugEnabled()
        return entityResponder
    }

    BulkControllerSupport<D> getBulkControllerSupport(){
        if (!bulkControllerSupport) this.bulkControllerSupport = BulkControllerSupport.of(getEntityClass())
        return bulkControllerSupport
    }

    /**
     * Gets the repository for the entityClass
     * @return The repository
     */
    GormRepo<D> getRepo() {
        //GrailsClassUtils.getStaticPropertyValue(getEntityClass(),'repo')
        (GormRepo<D>) InvokerHelper.invokeStaticMethod(getEntityClass(), 'getRepo', null)
    }

    /**
     * POST /api/entity
     * Create with data
     */
    @Action
    def post() {
        try {
            D instance = (D) getRepo().create(bodyAsMap())
            respondWithEntityMap(instance, getParamsMap(), CREATED)
        } catch (Exception e) {
            handleException(e)
        }
    }

    /**
     * PUT /api/entity/${id}
     * Update with data
     */
    @Action
    def put() {
        Map dataMap = bodyAsMap()
        Map data = [id: params.id] //this should be fine since grails isnt loosing the params set from UrlMappings
        data.putAll(dataMap) // json data may not contains id because it passed in params
        try {
            D instance = (D) getRepo().update(data)
            respondWithEntityMap(instance, getParamsMap())
        } catch (Exception e) {
            handleException(e)
        }

    }

    /**
     * DELETE /api/entity/${id}
     */
    @Action
    def delete() {
        try {
            getRepo().removeById((Serializable) params.id) //this should be fine since grails isnt loosing the params set from UrlMappings
            callRender(status: NO_CONTENT) //204
        } catch (Exception e) {
            handleException(e)
        }

    }

    /**
     * GET /api/entity/${id}
     */
    @Action
    def get() {
        try {
            Serializable idx = params.id as Serializable //this should be fine since grails isnt loosing the params set from UrlMappings
            D instance = (D) getRepo().read(idx)
            RepoUtil.checkFound(instance, idx, getEntityClass().simpleName)
            respondWithEntityMap(instance, getParamsMap())
        } catch (Exception e) {
            handleException(e)
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
            Map gParams = getParamsMap()
            log.debug("list with gParams ${gParams}")
            Pager pager = getEntityResponder().pagedQuery(gParams, [IncludesKey.list.name()])
            //we pass in the params to args so it can get passed on to renderer, used in the excel renderer for example
            respondWith(pager, [params: gParams])
        } catch (Exception e) {
            handleException(e)
        }
    }

    @Action
    def picklist() {
        try {
            Map gParams = getParamsMap()
            Pager pager = picklistPagedQuery(gParams)
            //we pass in the params to args so it can get passed on to renderer, used in the excel renderer for example
            respondWith(pager, [params: gParams])
        } catch (Exception e) {
            handleException(e)
        }
    }

    /** Used for bulk create calls when Job object is returned */
    @Action
    def bulkCreate() {
        try {
            bulkProcess(DataOp.add)
        } catch (Exception e) {
            respondWith getBulkControllerSupport().handleBulkOperationException(request, e)
        }
    }

    /** Used for bulk create calls when Job object is returned */
    @Action
    def bulkUpdate() {
        try {
            bulkProcess(DataOp.update)
        } catch (Exception e) {
            respondWith getBulkControllerSupport().handleBulkOperationException(request, e)
        }
    }

    void bulkProcess(DataOp dataOp) {
        List dataList = bodyAsList() as List<Map>
        Map gParams = getParamsMap()
        HttpServletRequest req = request
        String sourceId = "${req.method} ${req.requestURI}?${req.queryString}"
        SyncJobArgs syncJobArgs = getBulkControllerSupport().setupSyncJobArgs(dataOp, gParams, sourceId)
        SyncJobEntity job = getBulkControllerSupport().process(dataList, syncJobArgs)
        respondWith(job, [status: MULTI_STATUS])
    }

    void respondWithEntityMap(D instance, Map mParams, HttpStatus status = HttpStatus.OK){
        getEntityResponder().respondWith(this, instance, mParams, status)
    }

    /**
     * picklist has defaults of 50 for max and
     */
    Pager picklistPagedQuery(Map mParams) {
        mParams.max = mParams.max ?: getPicklistMax() //default to 50 for picklists
        return getEntityResponder().pagedQuery(mParams, ['picklist', IncludesKey.stamp.name()])
    }

    void handleException(Exception e) {
        //do nothing if its broken pipe, coz we can not write any byte to response at all.
        if(isBrokenPipe(e)) return
        else {
            assert getEntityClass()
            Problem apiError = problemHandler.handleException(getEntityClass(), e)
            respondWith(apiError)
        }
    }
}
