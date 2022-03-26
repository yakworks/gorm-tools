/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

import groovy.transform.CompileStatic

import org.springframework.http.HttpStatus

import gorm.tools.api.IncludesConfig
import gorm.tools.beans.AppCtx
import gorm.tools.beans.Pager
import gorm.tools.beans.map.MetaMap
import gorm.tools.beans.map.MetaMapEntityService
import gorm.tools.beans.map.MetaMapList
import gorm.tools.mango.api.QueryArgs
import gorm.tools.mango.api.QueryMangoEntityApi
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
    //common valida param keys to remove so that will not be considered a filter
    List<String> whitelistKeys =['controller', 'action', 'format', 'nd', '_search', 'includes', 'includesKey' ]

    IncludesConfig includesConfig
    MetaMapEntityService metaMapEntityService

    Class<D> entityClass
    String logicalName
    String namespace

    EntityResponder(Class<D> entityClass){
        this.entityClass = entityClass
    }

    EntityResponder(Class<D> entityClass, IncludesConfig includesConfig, MetaMapEntityService metaMapEntityService){
        this.entityClass = entityClass
        this.includesConfig = includesConfig
        this.metaMapEntityService = metaMapEntityService
    }

    public static <D> EntityResponder<D> of(Class<D> entityClass){
        def ic = AppCtx.get('includesConfig', IncludesConfig)
        def mes = AppCtx.get('metaMapEntityService', MetaMapEntityService)
        return new EntityResponder(entityClass, ic, mes)
    }

    /**
     * calls the IncludesConfig's getIncludes passing in any controller overrides
     */
    Map getIncludesMap(){
        assert includesConfig
        assert entityClass
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
        MetaMap emap = metaMapEntityService.createMetaMap(instance, incs)
        return emap
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
        def incMap = getIncludesMap()
        return IncludesConfig.getFieldIncludes(incMap, keyList)
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

    Pager pagedQuery(Map params, List<String> includesKeys) {
        Pager pager = new Pager(params)
        List dlist = query(pager, params)
        List<String> incs = findIncludes(params, includesKeys)
        MetaMapList entityMapList = metaMapEntityService.createMetaMapList(dlist, incs)
        return pager.setEntityMapList(entityMapList)
    }

    List<D> query(Pager pager, Map parms) {
        Map pclone = Maps.clone(parms) as Map<String, Object>
        //remove the fields that grails adds for controller and action
        pclone.removeAll {it.key in whitelistKeys }
        QueryArgs qargs = QueryArgs.of(pager).build(pclone)
        ((QueryMangoEntityApi)getRepo()).queryList(qargs)
    }
}
