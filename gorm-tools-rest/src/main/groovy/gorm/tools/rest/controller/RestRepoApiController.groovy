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

import gorm.tools.beans.EntityMap
import gorm.tools.beans.EntityMapList
import gorm.tools.beans.EntityMapService
import gorm.tools.beans.Pager
import gorm.tools.job.RepoJobEntity
import gorm.tools.job.RepoJobService
import gorm.tools.mango.api.QueryMangoEntityApi
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.bulk.BulkableArgs
import gorm.tools.repository.errors.api.ApiError
import gorm.tools.repository.errors.api.ApiErrorHandler
import gorm.tools.repository.model.DataOp
import gorm.tools.rest.RestApiConfig
import grails.web.Action

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.MULTI_STATUS
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

/**
 * This is the CRUD controller for entities
 * @param <D> Object
 */
@CompileStatic
@SuppressWarnings(['CatchRuntimeException'])
trait RestRepoApiController<D> extends RestApiController {

    //Need it to access log and still compile static in trait (See https://issues.apache.org/jira/browse/GROOVY-7439)
    final private static Logger log = LoggerFactory.getLogger(RestRepoApiController)

    @Autowired
    RestApiConfig restApiConfig

    // @Resource MessageSource messageSource

    @Autowired
    EntityMapService entityMapService

    @Autowired
    ApiErrorHandler apiErrorHandler

    @Autowired(required = false)
    RepoJobService repoJobService

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
            Map dataMap = parseJson(request)
            D instance = (D) getRepo().create(dataMap)
            def entityMap = createEntityMap(instance)
            respondWithEntityMap(entityMap, [status: CREATED])
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
        Map dataMap = parseJson(request)
        Map data = [id: params.id]
        data.putAll(dataMap) // json data may not contains id because it passed in params
        try {
            D instance = (D) getRepo().update(data)
            def entityMap = createEntityMap(instance)
            respondWithEntityMap(entityMap, [status: OK])
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
            RepoUtil.checkFound(instance, params.id as  Serializable, entityClass.simpleName)
            def entityMap = createEntityMap(instance)
            respondWithEntityMap(entityMap)
            // respond(jsonObject)
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
        Pager pager = pagedQuery(params, 'list')
        // passing renderArgs args would be usefull for 'renderNulls' if we want to include/exclude
        respond([view: '/object/_pagedList'], [pager: pager, renderArgs: [:]])
        // respond query(params)
    }

    @Action
    def picklist() {
        Pager pager = pagedQuery(params, 'picklist')
        Map renderArgs = [:]
        respond([view: '/object/_pagedList'], [pager: pager, renderArgs: renderArgs])
    }

    // @Action
    // def bulkUpdate() {
    //     Map dataMap = parseJson(request)
    //     List<Map> data = getRepo().bulkUpdate(dataMap.ids as List, dataMap.data as Map)
    //     respond([data: data])
    // }

    // XXX we don't have incs. We just need to do entityMap and respond it
    //  for returning Job we need to figure out what to do with bytes[] data and how to return Results associations.
    //  We need special method for that. Maybe something like we return error (list of ApiError ) but also we need to return
    //  list of all sourceIds/ids that we created
    //  so in includes we can specify what we return
    //FIXME #339 we need to see if we can rethink this.
    // in bulkCreate we convert the object to json bytes ( need to save to db so have to do this)
    // then here we pull the json bytes from the jsonB and turn it back into object (here we can optimize)
    // and then repond is going to take that object and turn it back into json bytes
    // seems we should be able to skip some steps here somehow.

    /** Used for bulk create calls when Job object is returned */
    @Action
    def bulkCreate() {
        bulkProcess(request, params, DataOp.add)
    }

    /** Used for bulk create calls when Job object is returned */
    @Action
    def bulkUpdate() {
        bulkProcess(request, params, DataOp.update)
    }


    def bulkProcess(HttpServletRequest req, Map params, DataOp dataOp) {
        List dataList = parseJsonList(req)
        String sourceKey = "${req.method} ${req.requestURI}?${req.queryString}"
        // String contextPath = req.getContextPath()
        // String requestURL = req.getRequestURL()
        // String forwardURI = req.forwardURI
        // XXX for now default is false, but we should change
        boolean asyncEnabled = params.asyncEnabled ? params.asyncEnabled as Boolean : false
        Map bulkParams = [sourceId: sourceKey, source: params.jobSource]
        List bulkIncludes = getIncludesMap()['bulk'] as List
        BulkableArgs bulkableArgs = new BulkableArgs(op: dataOp, includes: bulkIncludes, params: bulkParams, asyncEnabled: asyncEnabled)

        Long jobId = getRepo().bulk(dataList, bulkableArgs)
        RepoJobEntity job = repoJobService.getJob(jobId)

        //respondWithEntityMap(entityMapService.createEntityMap(job, null), [status: CREATED])
        Map resp = [id: job.id, ok:job.ok, state:job.state.name(), data: (job.data ? parseJsonBytes(job.data) : []), source:job.source, sourceId:job.sourceId]
        respond resp, status: MULTI_STATUS.value()
    }

    //@CompileDynamic
    Pager pagedQuery(Map params, String includesKey) {
        Pager pager = new Pager(params)
        // println "params ${params.class} $params"
        List dlist = query(pager, params)
        List incs = getFieldIncludes(includesKey)
        EntityMapList entityMapList = entityMapService.createEntityMapList(dlist, incs)
        return pager.setEntityMapList(entityMapList)
    }

    List<D> query(Pager pager, Map p = [:]) {
        //copy the params into new map
        def qryParams = p.findAll {
            //not if its in the the pager or the controller params
            !(it.key in ['max', 'offset', 'page', 'controller', 'action'])
        }
        qryParams.pager = pager
        //setup quick search
        List qsFields = getIncludesMap()['qSearch'] as List
        if(qsFields) qryParams.qSearchFields = qsFields

        ((QueryMangoEntityApi)getRepo()).queryList(qryParams)
    }

    void respondWithEntityMap(EntityMap entityMap, Map args = [:]){
        def resArgs = [view: '/object/_entityMap'] // as Map<String, Object>
        if(args) resArgs.putAll(args)
        respond(resArgs as Map, [entityMap: entityMap] as Map)
    }

    /**
     * builds the response model with the EntityMap wrapper.
     *
     * @param instance the entity instance
     * @param includeKey the key to use in the includes map, use default by default
     * @return the object to pass on to json views
     */
    EntityMap createEntityMap(D instance, String includesKey = 'get'){
        List incs = getFieldIncludes(includesKey)
        // def emap = BeanPathTools.buildMapFromPaths(instance, incs)
        EntityMap emap = entityMapService.createEntityMap(instance, incs)
        return emap
    }

    Map getIncludesMap(){
        //we are in trait, always use getters in case they are overrriden in implementing class
        return getRestApiConfig().getIncludes(getControllerName(), getNamespaceProperty(), getEntityClass(), getIncludes())
    }

    /**
     * get the fields includes, returns 'get' as the default if nothing found for includesKey
     */
    List<String> getFieldIncludes(String includesKey){
        //we are in trait, always use getters in case they are overrriden in implementing class
        def includesMap = getIncludesMap()
        List incs = (includesMap[includesKey] ?: includesMap['get'] ) as List<String>
        return incs
    }

    /**
     * implementing controller class can provide the includes map property.
     * This will override whats in the entity and the config
     */
    Map getIncludes(){ [:] }

    void handleException(Exception e) {
        ApiError apiError = apiErrorHandler.handleException(entityClass, e)

        respond([view: '/errors/_apiError', status: apiError.status.value()], apiError)
    }

}
