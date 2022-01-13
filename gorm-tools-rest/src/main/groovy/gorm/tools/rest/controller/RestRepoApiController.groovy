/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

import javax.servlet.http.HttpServletRequest

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
import gorm.tools.beans.map.MetaMap
import gorm.tools.beans.map.MetaMapEntityService
import gorm.tools.beans.map.MetaMapList
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.mango.api.QueryArgs
import gorm.tools.mango.api.QueryMangoEntityApi
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.DataOp
import grails.web.Action
import yakworks.commons.lang.EnumUtils
import yakworks.commons.map.Maps
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

    @Autowired
    IncludesConfig includesConfig

    // @Resource MessageSource messageSource

    @Autowired
    MetaMapEntityService metaMapEntityService


    @Autowired(required = false)
    SyncJobService syncJobService

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
            RepoUtil.checkFound(instance, params.id as Serializable, entityClass.simpleName)
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
            Pager pager = pagedQuery(params, [IncludesKey.list.name()])
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
        HttpServletRequest req = request

        String sourceKey = "${req.method} ${req.requestURI}?${req.queryString}"
        // FIXME for now default is false, but we should change
        boolean promiseEnabled = paramBoolean('promiseEnabled', false)

        List bulkIncludes = getIncludesMap()[IncludesKey.bulk.name()] as List
        SyncJobArgs syncJobArgs = new SyncJobArgs(op: dataOp, includes: bulkIncludes,
            sourceId: sourceKey, source: params.jobSource, promiseEnabled: promiseEnabled)
        //Can override payload storage or turn off with 'NONE' if not needed for big loads
        syncJobArgs.savePayload = Maps.getBoolean('savePayload', params, true)
        syncJobArgs.saveDataAsFile = Maps.getBoolean('saveDataAsFile', params)

        doBulk(dataList, syncJobArgs)
    }

    void doBulk(List<Map> dataList, SyncJobArgs syncJobArgs){
        Long jobId = getRepo().bulk(dataList, syncJobArgs)
        SyncJobEntity job = syncJobService.getJob(jobId)
        respondWith(job, [status: MULTI_STATUS])
    }

    void respondWithEntityMap(D instance, HttpStatus status = HttpStatus.OK){
        MetaMap entityMap = createEntityMap(instance)
        respondWith(entityMap, [status: status])
    }

    /**
     * picklist has defaults of 50 for max and
     */
    Pager picklistPagedQuery(Map params) {
        params.max = params.max ?: getPicklistMax() //default to 50 for picklists
        return pagedQuery(params, ['picklist', IncludesKey.stamp.name()])
    }

    Pager pagedQuery(Map params, List<String> includesKeys) {
        Pager pager = new Pager(params)
        List dlist = query(pager, params)
        List<String> incs = findIncludes(params, includesKeys)
        MetaMapList entityMapList = metaMapEntityService.createMetaMapList(dlist, incs)
        return pager.setEntityMapList(entityMapList)
    }

    /**
     * finds the right includes.
     *   - looks for includes param and uses that if passed in
     *   - looks for includesKey param and uses that if set, falling back to the defaultIncludesKey
     *   - falls back to the passed fallbackKeys if not set
     *   - the fallbackKeys will itself unlimately fallback to the 'get' includes if it can't be found
     *
     * @param params the request params
     * @return the List of includes field that can be passed to the MetaMap creation
     */
    List<String> findIncludes(Map params, List<String> fallbackKeys = []){
        List<String> keyList = []
        //if it has a includes then just parse that and pass it back
        if(params.containsKey('includes')) {
            return (params['includes'] as String).tokenize(',')*.trim()
        } else if(params.containsKey('includesKey')){
            keyList << (params['includesKey'] as String)
        }
        keyList.addAll(fallbackKeys)
        return IncludesConfig.getFieldIncludes(getIncludesMap(), keyList)
    }

    List<D> query(Pager pager, Map parms) {
        QueryArgs qargs = QueryArgs.of(pager).build(parms)
        ((QueryMangoEntityApi)getRepo()).queryList(qargs)
    }

    /**
     * builds the response model with the EntityMap wrapper.
     *
     * @param instance the entity instance
     * @param includeKey the key to use in the includes map, use default by default
     * @return the object to pass on to json views
     */
    MetaMap createEntityMap(D instance){
        flushIfSession() //in testing need to flush before generating entitymap
        List<String> incs = findIncludes(params)
        MetaMap emap = metaMapEntityService.createMetaMap(instance, incs)
        return emap
    }

    /**
     * In rare cases controller action will be inside a hibernate session
     * primarily needed for testing but there are some edge cases where this is needed
     * checks if repo datastore has a session and flushes if so
     */
    void flushIfSession(){
        if(getRepo().datastore.hasCurrentSession()){
            getRepo().flush()
        }
    }

    /**
     * calls the IncludesConfig's getIncludes passing in any controller overrides
     */
    Map getIncludesMap(){
        //we are in trait, always use getters in case they are overrriden in implementing class
        return getIncludesConfig().getIncludes(getControllerName(), getNamespaceProperty(), getEntityClass(), [:])
    }

    /**
     * calls IncludesConfig.getFieldIncludes with this controllers getIncludesMap()
     */
    List<String> getFieldIncludes(List<String> includesKeys){
        return IncludesConfig.getFieldIncludes(getIncludesMap(), [IncludesKey.get.name()])
    }

    void handleException(Exception e) {
        assert getEntityClass()
        ProblemTrait apiError = problemHandler.handleException(getEntityClass(), e)
        respondWith(apiError)
    }

}
