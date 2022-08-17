/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

import gorm.tools.api.IncludesConfig
import gorm.tools.beans.AppCtx
import gorm.tools.beans.Pager
import gorm.tools.mango.api.QueryArgs
import gorm.tools.mango.api.QueryMangoEntityApi
import gorm.tools.metamap.MetaMap
import gorm.tools.metamap.MetaMapList
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoLookup
import grails.web.api.WebAttributes
import yakworks.commons.map.Maps

/**
 * Helpers for a Restfull api type controller.
 * see grails-core/grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy
 */
@CompileStatic
class EntityResponder<D> {
    //common valida param keys to remove so that will not be considered a filter //TODO move this to external config
    List<String> whitelistKeys = ['controller', 'action', 'format', 'nd', '_search', 'includes', 'includesKey' ]

    @Autowired(required = false)
    IncludesConfig includesConfig

    @Autowired(required = false)
    MetaMapService metaMapService

    Class<D> entityClass
    String logicalName
    String namespace

    EntityResponder(Class<D> entityClass){
        this.entityClass = entityClass
    }

    EntityResponder(Class<D> entityClass, IncludesConfig includesConfig, MetaMapService metaMapService){
        this.entityClass = entityClass
        this.includesConfig = includesConfig
        this.metaMapService = metaMapService
    }

    public static <D> EntityResponder<D> of(Class<D> entityClass){
        def erInstance = new EntityResponder(entityClass)
        AppCtx.autowire(erInstance)
        return erInstance
    }

    /**
     * calls the IncludesConfig's getIncludes passing in any controller overrides
     */
    Map getIncludesMap(){
        return includesConfig.getIncludes(entityClass)
    }

    /**
     * respond with instance, calls ctrl.respondWith after it creates and entityMap
     */
    void respondWith(RestRegistryResponder ctrl, D instance, HttpStatus status = HttpStatus.OK){
        MetaMap entityMap = createEntityMap(instance, (ctrl as WebAttributes).params)
        ctrl.respondWith(entityMap, [status: status])
    }

    /**
     * builds the response model with the EntityMap wrapper.
     *
     * @param instance the entity instance
     * @param params the the param map to lookup the includes on
     * @return the object to pass on to json views
     */
    MetaMap createEntityMap(Object instance, Map params){
        flushIfSession() //in testing need to flush before generating entitymap
        List<String> incs = findIncludes(params)
        MetaMap emap = metaMapService.createMetaMap(instance, incs)
        return emap
    }

    Pager pagedQuery(Map params, List<String> includesKeys) {
        Pager pager = new Pager(params)
        List dlist = query(pager, params)
        List<String> incs = findIncludes(params, includesKeys)
        MetaMapList entityMapList = metaMapService.createMetaMapList(dlist, incs)
        return pager.setEntityMapList(entityMapList)
    }

    List<D> query(Pager pager, Map parms) {
        Map pclone = Maps.clone(parms) as Map<String, Object>
        //remove the fields that grails adds for controller and action
        pclone.removeAll {it.key in whitelistKeys }
        QueryArgs qargs = QueryArgs.of(pager).build(pclone)
        ((QueryMangoEntityApi)getRepo()).queryList(qargs)
    }

    /**
     * calls includesConfig.findIncludes. See javadocs there for more info
     */
    List<String> findIncludes(Map params, List<String> fallbackKeys = []){
        return includesConfig.findIncludes(entityClass.name, params, fallbackKeys)
    }

    /**
     * Gets the repository for the entityClass
     * @return The repository
     */
    GormRepo<D> getRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

    /**
     * In certain rare cases controller action will be inside a hibernate session
     * primarily needed for testing but there are some edge cases where this is needed
     * checks if repo datastore has a session and flushes if so
     */
    void flushIfSession(){
        if(getRepo().datastore.hasCurrentSession()){
            getRepo().flush()
        }
    }
}
