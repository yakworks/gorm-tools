/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import gorm.tools.Pager
import gorm.tools.beans.EntityMap
import gorm.tools.beans.EntityMapFactory
import gorm.tools.repository.GormRepoEntity
import gorm.tools.repository.RepoMessage
import gorm.tools.repository.api.RepositoryApi
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.rest.RestApiConfig
import grails.artefact.controller.RestResponder
import grails.artefact.controller.support.ResponseRenderer
import grails.databinding.SimpleMapDataBindingSource
import grails.util.GrailsNameUtils
import grails.validation.ValidationException
import grails.web.Action
import grails.web.api.ServletAttributes
import grails.web.databinding.DataBindingUtils

import static org.springframework.http.HttpStatus.CONFLICT
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

@CompileStatic
@SuppressWarnings(['CatchRuntimeException', 'NoDef'])
trait RestRepositoryApi<D extends GormRepoEntity> implements RestResponder, ServletAttributes, MangoControllerApi {

    @Autowired
    RestApiConfig restApiConfig

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
    RepositoryApi<D> getRepo() {
        //GrailsClassUtils.getStaticPropertyValue(getEntityClass(),'repo')
        (RepositoryApi<D>) InvokerHelper.invokeStaticMethod(getEntityClass(), 'getRepo', null)
    }

    /**
     * POST /api/entity
     * Create with data
     */
    @Action
    def post() {
        try {
            D instance = (D) getRepo().create(getDataMap())
            def entityMap = createEntityMap(instance)
            respondWithEntityMap(entityMap, [status: CREATED])
        } catch (RuntimeException e) {
            handleException(e)
        }
    }

    /**
     * PUT /api/entity/${id}* Update with data
     */
    @Action
    def put() {
        Map data = [id: params.id]
        data.putAll(getDataMap()) // getDataMap doesnt contains id because it passed in params
        try {
            D instance = (D) getRepo().update(data)
            def entityMap = createEntityMap(instance)
            respondWithEntityMap(entityMap, [status: OK])
        } catch (RuntimeException e) {
            handleException(e)
        }

    }

    /**
     * DELETE /api/entity/${id}* update with params
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
     * GET /api/entity/${id}* update with params
     */
    @Action
    def get() {
        try {
            D instance = (D) getRepo().get(params)
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
    def listPost() {
        respond query((request.JSON ?: [:]) as Map, params)
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
    def pickList() {
        Pager pager = pagedQuery(params, 'pickList')
        Map renderArgs = [:]
        respond([view: '/object/_pagedList'], [pager: pager, renderArgs: renderArgs])
    }

    //@CompileDynamic
    Pager pagedQuery(Map params, String includesKey) {
        Pager pager = new Pager(params)
        // println "params ${params.class} $params"
        List dlist = query(pager, params)
        List incs = getIncludes(includesKey)
        return pager.setupList(dlist, incs)
    }

    List query(Pager pager, Map p = [:]) {
        ['max', 'offset', 'page'].each{ String k ->
            p[k] = pager[k]
        }

        //def qSearch = p.remove('q')
        if(p.q && getSearchFields()) {
            Map qMap = ['text': p.q, 'fields': getSearchFields()]
            p['$q'] = qMap
        }

        getMangoApi().query(p)
    }

    void respondWithEntityMap(EntityMap entityMap, Map args = [:]){
        def resArgs = [view: '/object/_entityMap'] as Map<String, Object>
        if(args) resArgs.putAll(args)
        respond(resArgs, [entityMap: entityMap])
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
        EntityMap emap = EntityMapFactory.createEntityMap(instance, incs)
        return emap
    }

    //@SuppressWarnings(['ReturnsNullInsteadOfEmptyCollection'])

    List getIncludes(String includesKey){
        //we are in trait, always use getters in case they are overrriden in implementing class
        def includesMap = getRestApiConfig().getIncludes(getControllerName(), getEntityClass(), getIncludes())
        List incs = (includesMap[includesKey] ?: includesMap['get'] ) as List
        return incs
    }

    List getSearchFields(){
        //we are in trait, always use getters in case they are overrriden in implementing class
        def qincs = getRestApiConfig().getQSearchIncludes(getControllerName(), getEntityClass(), getqSearchIncludes())
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

    /**
     * getControllerName() works inisde a request and should be used, but during init or outside a request use this
     * should give roughly what logicalName is which is used to setup the urlMappings by default
     */
    String getLogicalControllerName(){
        String logicalName = GrailsNameUtils.getLogicalName(this.class, 'Controller')
        return GrailsNameUtils.getPropertyName(logicalName)
    }

    /**
     * The Map object that can be bound to create or update domain entity.  Defaults whats in the request based on mime-type.
     * Subclasses may override this
     */
    @CompileDynamic //so it can access the SimpleMapDataBindingSource.map
    Map getDataMap() {
        SimpleMapDataBindingSource bsrc =
                (SimpleMapDataBindingSource) DataBindingUtils.createDataBindingSource(grailsApplication, getEntityClass(), getRequest())
        return bsrc.map
    }

    /**
     * Cast this to ResponseRenderer and call render
     * @param args
     */
    void callRender(Map args) {
        ((ResponseRenderer) this).render args
    }

    void callRender(Map argMap, CharSequence body){
        ((ResponseRenderer) this).render(argMap, body)
    }

    /**
     * CAst this to RestResponder and call respond
     * @param value
     * @param args
     */
    def callRespond(Object value, Map args = [:]) {
        ((RestResponder) this).respond value, args
    }

    void handleException(RuntimeException e) {
        //log.error e.message
        if( e instanceof EntityNotFoundException){
            callRender(status: NOT_FOUND, e.message)
        }
        else if( e instanceof EntityValidationException ){
            String m = buildMsg(e.messageMap, e.errors)
            // log.info m
            callRender(status: UNPROCESSABLE_ENTITY, m)
        }
        else if( e instanceof ValidationException ){
            String m = buildMsg([defaultMessage: e.message], e.errors)
            callRender(status: UNPROCESSABLE_ENTITY, m)
        }
        else if( e instanceof OptimisticLockingFailureException ){
            callRender(status: CONFLICT, e.message)
        } else {
            throw e
        }

    }

    String buildMsg(Map msgMap, Errors errors) {
        StringBuilder result = new StringBuilder(msgMap['defaultMessage'] as String)
        // FIXME not sure where msg comes from
        // errors.getAllErrors().each { error ->
        //     error =  error as FieldError
        //     String msg = "\n${message(error: error, args: error.arguments, local: RepoMessage.defaultLocale())}"
        //     result.append("\n" + message(error: error, args: error.arguments, local: RepoMessage.defaultLocale()))
        // }
        return result
    }

}
