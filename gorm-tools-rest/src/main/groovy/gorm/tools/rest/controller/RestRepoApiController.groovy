/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.InvokerHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver
import org.springframework.http.HttpStatus

import gorm.tools.api.IncludesConfig
import gorm.tools.api.IncludesKey
import gorm.tools.beans.Pager
import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.DataOp
import grails.web.Action
import yakworks.problem.ProblemTrait

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
        post: "POST", put: ["PUT", "POST"], bulkUpdate: "POST", bulkCreate: "POST", delete: "DELETE"] //patch: "PATCH",

    //Need it to access log and still compile static in trait (See https://issues.apache.org/jira/browse/GROOVY-7439)
    final private static Logger log = LoggerFactory.getLogger(RestRepoApiController)

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
            respondWithEntityMap(instance, CREATED)
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
        Map data = [id: params.id]
        data.putAll(dataMap) // json data may not contains id because it passed in params
        try {
            D instance = (D) getRepo().update(data)
            respondWithEntityMap(instance)
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
            getRepo().removeById((Serializable) params.id)
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
            D instance = (D) getRepo().read(params.id as Serializable)
            RepoUtil.checkFound(instance, params.id as Serializable, getEntityClass().simpleName)
            respondWithEntityMap(instance)
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
            Pager pager = getEntityResponder().pagedQuery(params, [IncludesKey.list.name()])
            respondWith pager
        } catch (Exception e) {
            handleException(e)
        }
    }

    @Action
    def picklist() {
        try {
            Pager pager = picklistPagedQuery(params)
            respondWith pager
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
            handleException(e)
        }
    }

    /** Used for bulk create calls when Job object is returned */
    @Action
    def bulkUpdate() {
        try {
            bulkProcess(DataOp.update)
        } catch (Exception e) {
            handleException(e)
        }
    }

    void bulkProcess(DataOp dataOp) {
        List dataList = bodyAsList() as List<Map>
        SyncJobEntity job = getBulkControllerSupport().process(dataOp, dataList, webRequest)
        respondWith(job, [status: MULTI_STATUS])
    }

    void respondWithEntityMap(D instance, HttpStatus status = HttpStatus.OK){
        getEntityResponder().respondWith(this, instance, status)
    }

    /**
     * picklist has defaults of 50 for max and
     */
    Pager picklistPagedQuery(Map params) {
        params.max = params.max ?: getPicklistMax() //default to 50 for picklists
        return getEntityResponder().pagedQuery(params, ['picklist', IncludesKey.stamp.name()])
    }

    void handleException(Exception e) {
        assert getEntityClass()
        ProblemTrait apiError = problemHandler.handleException(getEntityClass(), e)
        respondWith(apiError)
    }

}
