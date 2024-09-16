/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api

import groovy.json.JsonException
import groovy.transform.CompileStatic

import org.grails.orm.hibernate.HibernateDatastore
import org.hibernate.QueryException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException

import gorm.tools.beans.Pager
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobEntity
import gorm.tools.mango.api.QueryArgs
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.ApiCrudRepo
import gorm.tools.repository.model.DataOp
import gorm.tools.transaction.TrxUtils
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.NotFoundProblem
import yakworks.gorm.api.support.BulkSupport
import yakworks.gorm.api.support.QueryArgsValidator
import yakworks.meta.MetaMap
import yakworks.meta.MetaMapList
import yakworks.spring.AppCtx

/**
 * CRUD api for rest repo
 */
@CompileStatic
class DefaultCrudApi<D> implements CrudApi<D> {

    Class<D> entityClass

    @Autowired HibernateDatastore hibernateDatastore
    @Autowired IncludesConfig includesConfig
    @Autowired ApiConfig apiConfig
    @Autowired MetaMapService metaMapService
    @Autowired QueryArgsValidator queryArgsValidator

    /** Not required but if an BulkSupport bean is setup then it will get get used */
    @Autowired(required = false)
    BulkSupport<D> bulkSupport

    DefaultCrudApi(Class<D> entityClass){
        this.entityClass = entityClass
    }

    public static <D> DefaultCrudApi<D> of(Class<D> entityClass){
        def capiInstance = new DefaultCrudApi(entityClass)
        AppCtx.autowire(capiInstance)
        //erInstance.pathItem = erInstance.apiConfig.pathsByEntity[entityClass.name]
        return capiInstance
    }

    /**
     * Gets the repository for the entityClass
     * @return The repository
     */
    ApiCrudRepo<D> getApiCrudRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

    //FIXME temporary until we refactor
    GormRepo<D> getRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

    BulkSupport<D> getBulkSupport(){
        if (!bulkSupport) this.bulkSupport = BulkSupport.of(getEntityClass())
        return bulkSupport
    }

    /**
     * calls the IncludesConfig's getIncludes passing in any controller overrides
     */
    Map getIncludesMap(){
        return includesConfig.getIncludes(entityClass)
    }

    /**
     * gets the entity by id
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    @Override
    Map get(Serializable id, Map qParams){
        D instance = (D) getApiCrudRepo().read(id)
        RepoUtil.checkFound(instance, id, getEntityClass().simpleName)
        Map eMap = entityToMap(instance, qParams)
        return eMap
    }

    /**
     * Create entity from data and return the MetaMap of what was created
     */
    @Override
    Map create(Map data, Map qParams){
        Boolean bindId = qParams.getBoolean('bindId', false)
        var args = PersistArgs.of(bindId: bindId)
        D instance = (D) getApiCrudRepo().create(data, args)
        Map eMap = entityToMap(instance, qParams)
        return eMap
    }

    /**
     * Update entity from data
     */
    @Override
    Map update(Map data, Map qParams){
        Map dataMap = [id: qParams.id]
        // json dataMap will normally not contain id because it passed in url params,
        // but if it does it copies it in and overrides, so the id in the dataMap will win
        // FIXME I dont think the above is the right default, the url id I think should always win
        dataMap.putAll(data)
        D instance = (D) getApiCrudRepo().update(dataMap, PersistArgs.of())
        Map eMap = entityToMap(instance, qParams)
        return eMap
    }

    /**
     * Create or Update entity from data. Checks if key exists and updates, otherwise inserts
     */
    @Override
    Map upsert(Map data, Map qParams){
        //TODO
        return [:]
    }

    /**
     * Remove by ID
     * @param id - the id to delete
     * @param args - the PersistArgs to pass to delete. flush being the most common
     *
     * @throws NotFoundProblem.Exception if its not found or DataProblemException if a DataIntegrityViolationException is thrown
     */
    @Override
    void removeById(Serializable id, Map qParams){
        getApiCrudRepo().removeById(id)
    }

    /**
     * Checks if the id exists
     */
    @Override
    boolean exists(Serializable id){
        getApiCrudRepo().exists(id)
    }

    /**
     * Mango Query that returns a paged list
     */
    @Override
    Pager list(Map qParams, List<String> includesKeys){
        try {
            Pager pager = Pager.of(qParams)
            List dlist = queryList(pager, qParams)
            List<String> incs = findIncludes(qParams, includesKeys)
            MetaMapList entityMapList = metaMapService.createMetaMapList(dlist, incs)
            pager.setMetaMapList(entityMapList)
            return pager
        } catch (JsonException | IllegalArgumentException | QueryException ex) {
            //See #1925 - Catch bad query in 'q' parameter and report back. So we dont pollute logs, and can differentiate that its not us.
            //Hibernate throws IllegalArgumentException when Antlr fails to parse query
            //and throws QueryException when hibernate fails to execute query
            throw DataProblem.ex("Invalid query $ex.message")
        } catch (DataAccessException ex) {
            throw DataProblem.of(ex).toException()
        }
    }

    @Override
    SyncJobEntity bulk(DataOp dataOp, List<Map> dataList, Map qParams, String sourceId){
        SyncJobArgs syncJobArgs = getBulkSupport().setupSyncJobArgs(dataOp, qParams, sourceId)
        SyncJobEntity job = getBulkSupport().process(dataList, syncJobArgs)
        return job
    }

    protected List<D> queryList(Pager pager, Map qParams) {
        QueryArgs qargs = createQueryArgs(pager, qParams)
        //if (debugEnabled) log.debug("QUERY ${entityClass.name} queryArgs.criteria: ${qargs.buildCriteria()}")
        return getApiCrudRepo().query(qargs, null).pagedList(qargs.pager)
    }

    protected QueryArgs createQueryArgs(Pager pager, Map qParams) {
        QueryArgs qargs = QueryArgs.of(pager)
            .qRequired(qRequired())
            .build(qParams)
            .defaultSortById()
            .validateQ()

        validateQueryArgs(qargs)

        return qargs
    }

    /**
     * Converts the instance to Map using the MetaMap wrapper with metaMapService.
     *
     * @param instance the entity instance
     * @param params the the param map to lookup the includes on
     * @return the object to pass on to json views
     */
    @Override
    Map entityToMap(D instance, Map qParams){
        flushIfSession() //in testing need to flush before generating MetaMap
        List<String> incs = findIncludes(qParams)
        MetaMap emap = metaMapService.createMetaMap(instance, incs)
        return emap
    }

    void validateQueryArgs(QueryArgs args) {
        queryArgsValidator.validate(args)
    }

    protected boolean qRequired(){
        PathItem pathItem = apiConfig.pathsByEntity[entityClass.name]
        return pathItem?.qRequired
    }

    /**
     * calls includesConfig.findIncludes. See javadocs there for more info
     */
    protected List<String> findIncludes(Map qParams, List<String> fallbackKeys = []){
        return includesConfig.findIncludes(entityClass.name, qParams, fallbackKeys)
    }

    /**
     * In certain rare cases controller action will be inside a hibernate session
     * primarily needed for testing but there are some edge cases where this is needed
     * checks if repo datastore has a session and flushes if so
     */
    protected void flushIfSession(){
        if(hibernateDatastore.hasCurrentSession()){
            TrxUtils.flush(hibernateDatastore)
        }
    }
}
