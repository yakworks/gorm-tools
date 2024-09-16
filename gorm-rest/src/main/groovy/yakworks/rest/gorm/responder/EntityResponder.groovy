/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.responder

import groovy.json.JsonException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.hibernate.QueryException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException

import gorm.tools.beans.Pager
import gorm.tools.mango.api.QueryArgs
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoLookup
import grails.gorm.transactions.Transactional
import yakworks.api.problem.data.DataProblem
import yakworks.gorm.api.ApiConfig
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.PathItem
import yakworks.gorm.api.support.QueryArgsValidator
import yakworks.meta.MetaMap
import yakworks.meta.MetaMapList
import yakworks.spring.AppCtx

/**
 * Helpers for a Restfull api type controller.
 * see grails-core/grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy
 *
 * NO LONGER IN USE, SEE CrudApi
 */
@Deprecated
@Slf4j
@CompileStatic
class EntityResponder<D> {

    @Autowired IncludesConfig includesConfig
    @Autowired ApiConfig apiConfig
    @Autowired MetaMapService metaMapService
    //@Autowired List<EntityResponderValidator> entityResponderValidators
    @Autowired QueryArgsValidator queryArgsValidator

    Class<D> entityClass

    /** the API path item this is for*/
    PathItem pathItem

    //allows it to be turned on and off for controller
    boolean debugEnabled = false

    EntityResponder(Class<D> entityClass){
        this.entityClass = entityClass
    }

    // EntityResponder(Class<D> entityClass, IncludesConfig includesConfig, MetaMapService metaMapService){
    //     this.entityClass = entityClass
    //     this.includesConfig = includesConfig
    //     this.metaMapService = metaMapService
    // }

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
     * builds the response model with the MetaMap wrapper.
     *
     * @param instance the entity instance
     * @param params the the param map to lookup the includes on
     * @return the object to pass on to json views
     */
    MetaMap createEntityMap(Object instance, Map params){
        flushIfSession() //in testing need to flush before generating MetaMap
        List<String> incs = findIncludes(params)
        MetaMap emap = metaMapService.createMetaMap(instance, incs)
        return emap
    }

    @Transactional(readOnly = true)
    Pager pagedQuery(Map params, List<String> includesKeys) {
        Pager pager = Pager.of(params)
        List dlist = queryList(pager, params)
        List<String> incs = findIncludes(params, includesKeys)
        MetaMapList entityMapList = metaMapService.createMetaMapList(dlist, incs)
        pager.setMetaMapList(entityMapList)
        return pager
    }

    List<D> queryList(Pager pager, Map parms) {

        try {
            QueryArgs qargs = QueryArgs.of(pager)
            .qRequired(pathItem?.qRequired)
            .build(parms)
            .defaultSortById()
            .validateQ()

            validateQueryArgs(qargs)

            if (debugEnabled) log.debug("QUERY ${entityClass.name} queryArgs.criteria: ${qargs.buildCriteria()}")
            //getRepo().queryList(qargs, null, debugEnabled ? log : null)
            getRepo().query(qargs, null).pagedList(qargs.pager)
        } catch (JsonException | IllegalArgumentException | QueryException ex) {
            //See #1925 - Catch bad query in 'q' parameter and report back. So we dont pollute logs, and can differentiate that its not us.
            //Hibernate throws IllegalArgumentException when Antlr fails to parse query
            //and throws QueryException when hibernate fails to execute query
            throw DataProblem.ex("Invalid query $ex.message")
        } catch (DataAccessException ex) {
            throw DataProblem.of(ex).toException()
        }
    }

    void validateQueryArgs(QueryArgs args) {
        queryArgsValidator.validate(args)
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
