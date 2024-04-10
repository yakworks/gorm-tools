/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.controller

import groovy.json.JsonException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.hibernate.QueryException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus

import gorm.tools.beans.Pager
import gorm.tools.mango.api.QueryArgs
import gorm.tools.mango.api.QueryMangoEntityApi
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoLookup
import yakworks.api.problem.data.DataProblem
import yakworks.gorm.api.ApiConfig
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.PathItem
import yakworks.meta.MetaMap
import yakworks.meta.MetaMapList
import yakworks.spring.AppCtx

/**
 * Helpers for a Restfull api type controller.
 * see grails-core/grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy
 */
@Slf4j
@CompileStatic
class EntityResponder<D> {
    //common valida param keys to remove so that will not be considered a filter //TODO move this to external config
    List<String> whitelistKeys = ['controller', 'action', 'format', 'nd', '_search', 'includes', 'includesKey' ]

    @Autowired(required = false)
    IncludesConfig includesConfig

    @Autowired(required = false)
    ApiConfig apiConfig

    @Autowired(required = false)
    MetaMapService metaMapService

    Class<D> entityClass
    // String logicalName
    // String namespace
    /** the path item */
    PathItem pathItem
    //allows it to be turned on and off for controller
    boolean debugEnabled = false

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
        erInstance.pathItem = erInstance.apiConfig.pathsByEntity[entityClass.name]
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
    void respondWith(RestResponderTrait ctrl, D instance, Map params, HttpStatus status = HttpStatus.OK){
        MetaMap entityMap = createEntityMap(instance, params)
        ctrl.respondWith(entityMap, [status: status, params: params])
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

    Pager pagedQuery(Map params, List<String> includesKeys, boolean requireQ = false) {
        Pager pager = Pager.of(params)
        List dlist = query(pager, params)
        List<String> incs = findIncludes(params, includesKeys)
        MetaMapList entityMapList = metaMapService.createMetaMapList(dlist, incs)
        return pager.setMetaMapList(entityMapList)
    }

    List<D> query(Pager pager, Map parms) {
        // Map pclone = Maps.clone(parms) as Map<String, Object>
        //remove the fields that grails adds for controller and action
        // pclone.removeAll {it.key in whitelistKeys }
        try {
            QueryArgs qargs = QueryArgs.of(pager)
            .qRequired(pathItem?.qRequired)
            .build(parms)
            .defaultSortById()
            .validateQ()

            if (debugEnabled) log.debug("QUERY ${entityClass.name} queryArgs.criteria: ${qargs.buildCriteria()}")
            ((QueryMangoEntityApi) getRepo()).queryList(qargs, null, debugEnabled ? log : null)
        } catch (JsonException | IllegalArgumentException | QueryException ex) {
            //See #1925 - Catch bad query in 'q' parameter and report back. So we dont pollute logs, and can differentiate that its not us.
            //Hibernate throws IllegalArgumentException when Antlr fails to parse query
            //and throws QueryException when hibernate fails to execute query
            throw DataProblem.ex("Invalid query $ex.message")
        } catch (DataAccessException ex) {
            throw DataProblem.of(ex).toException()
        }
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
