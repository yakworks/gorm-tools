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

import gorm.tools.beans.EntityMap
import gorm.tools.beans.EntityMapList
import gorm.tools.beans.EntityMapService
import gorm.tools.beans.Pager
import gorm.tools.mango.api.QueryMangoEntityApi
import gorm.tools.repository.BulkableRepo
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.rest.RestApiConfig
import gorm.tools.rest.error.ApiError
import gorm.tools.rest.error.ApiErrorHandler
import gorm.tools.rest.error.ApiValidationError
import grails.web.Action

import static org.springframework.http.HttpStatus.*

/**
 * This is the CRUD controller for entities
 * @param <D> Object
 */
@CompileStatic
@SuppressWarnings(['CatchRuntimeException'])
trait RestRepositoryApi<D> implements RestApiController {

    //Need it to access log and still compile static in trait (See https://issues.apache.org/jira/browse/GROOVY-7439)
    final private static Logger log = LoggerFactory.getLogger(RestRepositoryApi.class)

    @Autowired
    RestApiConfig restApiConfig

    // @Resource MessageSource messageSource

    @Autowired
    EntityMapService entityMapService

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
        if (!entityClass) this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), RestRepositoryApi)
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
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
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

    @Action
    def bulkUpdate() {
        Map dataMap = parseJson(request)
        List<Map> data = getRepo().bulkUpdate(dataMap.ids as List, dataMap.data as Map)
        respond([data: data])
    }

    /** Used for bulk create calls when Job object is returned */
    @Action
    def bulkCreate() {
        List dataList = parseJsonList(request)
        def job = ((BulkableRepo)getRepo()).bulkCreate(dataList, [includes: getIncludes("bulk")])
        // XXX we don't have incs. We just need to do entityMap and respond it
        //  for returning Job we need to figure out what to do with bytes[] data and how to return Results associations.
        //  We need special method for that. Maybe something like we return error (list of ApiError ) but also we need to return
        //  list of all sourceIds/ids that we created
        //  so in includes we can specify what we return
        //respondWithEntityMap(entityMapService.createEntityMap(job, null), [status: CREATED])
        byte[] jsonB = job["results"] as byte[]
        String str = new String(jsonB, "UTF-8")
        Map resp = [id: job.id, ok:job.ok, results: parseJson(new StringReader(str))]
        respond resp, status: CREATED.value()
    }

    //@CompileDynamic
    Pager pagedQuery(Map params, String includesKey) {
        Pager pager = new Pager(params)
        // println "params ${params.class} $params"
        List dlist = query(pager, params)
        List incs = getIncludes(includesKey)
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
        List qsFields = getSearchFields()
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
        List incs = getIncludes(includesKey)
        // def emap = BeanPathTools.buildMapFromPaths(instance, incs)
        EntityMap emap = entityMapService.createEntityMap(instance, incs)
        return emap
    }

    List getIncludes(String includesKey){
        //we are in trait, always use getters in case they are overrriden in implementing class
        def includesMap = getRestApiConfig().getIncludes(getControllerName(), getNamespaceProperty(), getEntityClass(), getIncludes())
        List incs = (includesMap[includesKey] ?: includesMap['get'] ) as List
        return incs
    }

    List getSearchFields(){
        //we are in trait, always use getters in case they are overrriden in implementing class
        def qincs = getRestApiConfig().getQSearchIncludes(getControllerName(), getNamespaceProperty(), getEntityClass(), getqSearchIncludes())
        return qincs
    }

    /**
     * implementing class can provide the includes map property. using the restApi config is recomended
     */
    Map getIncludes(){ [:] }
    /**
     * implementing class can provide the qSearchIncludes property. using the restApi config is recomended
     */
    List getqSearchIncludes() { [] }

    void handleException(RuntimeException e) {
        ApiError apiError = ApiErrorHandler.handleException(entityClass, e)

        log.error(e.message, e)
        if( apiError instanceof ApiValidationError){
            respond([view: '/errors/_errors422'], apiError)
        } else {
            respond([view: '/errors/_errors'], apiError)
        }

    }

}
