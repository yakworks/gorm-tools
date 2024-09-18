/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api

import groovy.json.JsonException
import groovy.transform.CompileDynamic
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
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.ApiCrudRepo
import gorm.tools.repository.model.DataOp
import gorm.tools.transaction.TrxUtils
import grails.gorm.transactions.Transactional
import yakworks.api.problem.data.DataProblem
import yakworks.gorm.api.support.BulkApiSupport
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
    BulkApiSupport<D> bulkApiSupport

    DefaultCrudApi(Class<D> entityClass){
        this.entityClass = entityClass
    }

    public static <D> DefaultCrudApi<D> of(Class<D> entityClass){
        def capiInstance = new DefaultCrudApi(entityClass)
        AppCtx.autowire(capiInstance) //wire up the @Autowired
        //erInstance.pathItem = erInstance.apiConfig.pathsByEntity[entityClass.name]
        return capiInstance
    }

    class EntityResultImpl<D> implements EntityResult<D> {
        IncludesProps includesProps
        D entity

        EntityResultImpl(D entity, IncludesProps includesProps){
            this.entity = entity
            this.includesProps = includesProps
        }

        @CompileDynamic //getting goody error in intellij about casting entity to D
        Map asMap(){
            entityToMap(this.entity, includesProps)
        }
    }

    ApiCrudRepo<D> getApiCrudRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

    BulkApiSupport<D> getBulkApiSupport(){
        if (!bulkApiSupport) this.bulkApiSupport = BulkApiSupport.of(getEntityClass())
        return bulkApiSupport
    }

    /**
     * calls the IncludesConfig's getIncludes passing in any controller overrides
     */
    Map getIncludesMap(){
        return includesConfig.getIncludes(entityClass)
    }

    @Transactional(readOnly = true)
    @Override
    EntityResult<D> get(Serializable id, Map qParams){
        D instance = (D) getApiCrudRepo().read(id)
        RepoUtil.checkFound(instance, id, getEntityClass().simpleName)
        return createEntityResult(instance, qParams)
    }

    @Override
    EntityResult<D> createEntityResult(D instance, Map params){
        new EntityResultImpl(instance, IncludesProps.of(params))
    }

    /**
     * Create entity from data and return the MetaMap of what was created
     */
    @Transactional
    @Override
    EntityResult<D> create(Map data, Map qParams){
        Boolean bindId = qParams.getBoolean('bindId', false)
        var args = PersistArgs.of(bindId: bindId)
        D instance = (D) getApiCrudRepo().create(data, args)
        return createEntityResult(instance, qParams)
    }

    /**
     * Update entity from data
     */
    @Transactional
    @Override
    EntityResult<D> update(Map data, Map qParams){
        Map dataMap = [id: qParams.id]
        // json dataMap will normally not contain id because it passed in url params,
        // but if it does it copies it in and overrides, so the id in the dataMap will win
        // FIXME I dont think the above is the right default, the url id I think should always win
        dataMap.putAll(data)
        D instance = (D) getApiCrudRepo().update(dataMap, PersistArgs.of())
        return createEntityResult(instance, qParams)
    }

    /**
     * Create or Update entity from data. Checks if key exists and updates, otherwise inserts
     */
    @Transactional
    @Override
    EntityResult<D> upsert(Map data, Map qParams){
        //TODO
        return createEntityResult(null, qParams)
    }

    /**
     * Remove by ID
     * @param id - the id to delete
     * @param args - the PersistArgs to pass to delete. flush being the most common
     */
    @Transactional
    @Override
    void removeById(Serializable id, Map qParams){
        getApiCrudRepo().removeById(id)
    }

    /**
     * Checks if the id exists
     */
    @Transactional(readOnly = true)
    @Override
    boolean exists(Serializable id){
        getApiCrudRepo().exists(id)
    }

    /**
     * Mango Query that returns a paged list
     */
    @Transactional(readOnly = true)
    @Override
    Pager list(Map qParams, List<String> includesKeys){
        try {
            Pager pager = Pager.of(qParams)
            List dlist = queryList(pager, qParams)
            List<String> incs = includesConfig.findIncludes(entityClass.name, IncludesProps.of(qParams), includesKeys)
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
        SyncJobArgs syncJobArgs = getBulkApiSupport().setupSyncJobArgs(dataOp, qParams, sourceId)
        SyncJobEntity job = getBulkApiSupport().process(dataList, syncJobArgs)
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

    @Override
    Map entityToMap(D instance, IncludesProps includesProps){
        flushIfSession() //in testing need to flush before generating MetaMap
        List<String> incs = includesConfig.findIncludes(entityClass.name, includesProps, [])
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
