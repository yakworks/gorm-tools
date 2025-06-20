/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.QueryArgs
import gorm.tools.mango.api.QueryService
import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.RepoUtil
import yakworks.api.problem.data.NotFoundProblem
//NOTE: No Grail-Gorm dependencies here

/**
 * CRUD api for rest repo
 */
@CompileStatic
interface ApiCrudRepo<D> {

    Class<D> getEntityClass()

    QueryService getQueryService()

    /**
     * Inserts data, transactional wrap for doCreate
     */
    D create(Map data, PersistArgs args)

    default D create(Map data) {
        create(data, PersistArgs.of())
    }

    /**
     * Updates the data, Transactional wrap doUpdate
     */
    D update(Map data, PersistArgs args)

    default D update(Map data) {
        update(data, PersistArgs.defaults())
    }

    /**
     * Create or Update the data
     */
    EntityResult<D> upsert(Map data, PersistArgs args)

    default EntityResult<D> upsert(Map data) {
        upsert(data, PersistArgs.defaults())
    }

    /**
     * Remove by ID
     *
     * @param id - the id to delete
     * @param args - the args to pass to delete. flush being the most common
     *
     * @throws NotFoundProblem.Exception if its not found or DataProblemException if a DataIntegrityViolationException is thrown
     */
    void removeById(Serializable id, PersistArgs args)

    default void removeById(Serializable id) {
        removeById(id, PersistArgs.defaults())
    }

    /**
     * gets and verifies that the entity can be retrieved and version matches throwing error if not.
     *
     * @param id required, the id to get
     * @param version - can be null. if its passed in then it validates its that same as the version in the retrieved entity.
     * @return the retrieved entity. Will always be an entity as this throws an error if not
     *
     * @throws NotFoundProblem.Exception if its not found
     * @throws ValidationProblem.Exception if the versions mismatch
     */
    default D get(Serializable id, Long version) {
        D entity = get(id)
        RepoUtil.checkFound(entity, id, getEntityClass().name)
        if (version != null) RepoUtil.checkVersion(entity, version)
        return entity
    }

    /**
     * simple call to the gormStaticApi get, not in a trx to avoid overhead
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D get(Serializable id)

    /**
     * simple call to the gormStaticApi get, throws NotFoundProblem.Exception if checkAndThrow true
     *
     * @param id required, the id to get
     * @param checkAndThrow if true then checks if found and throws NotFoundProblem.Exception if not
     * @return the retrieved entity
     */
    default D get(Serializable id, boolean checkAndThrow) {
        D entity = get(id)
        if(checkAndThrow) RepoUtil.checkFound(entity, id, getEntityClass().name)
        return entity
    }

    /**
     * simple call to the gormStaticApi get, not in a trx to avoid overhead
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    List<D> getAll(List ids)

    /**
     * read only get
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D read(Serializable id)

    /**
     * load without hydrating
     *x
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D load(Serializable id)

    boolean exists(Serializable id)

    //-------------------- Bulk -------------------

    /**
     * creates a supplier to wrap doBulkParallel and calls bulk
     * if syncJobArgs.async = true will return right away
     *
     * @param dataList the list of data maps to create
     * @param syncJobArgs the args object to pass on to doBulk
     * @return Job id
     */
    // Long bulk(List<Map> dataList, SyncJobArgs syncJobArgs)
    //--------------------Mango Query -------------------

    /**
     * Primary method. Builds detached criteria for repository's domain based on mango criteria language and additional criteria
     * Override this one in repo for any special handling
     *
     * main method, all comes here so only need to override this one for custom logic
     *
     * @param queryArgs mango query args.
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    default MangoDetachedCriteria<D> query(QueryArgs queryArgs, @DelegatesTo(MangoDetachedCriteria)Closure closure) {
        getQueryService().query(queryArgs, closure)
    }

    default MangoDetachedCriteria<D> query(QueryArgs queryArgs) {
        query(queryArgs, null)
    }

    /**
     * Builds detached criteria for repository's domain based on mango criteria language and additional criteria
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    default MangoDetachedCriteria<D> query(Map params, @DelegatesTo(MangoDetachedCriteria)Closure closure) {
        query(QueryArgs.of(params), closure)
    }

    default MangoDetachedCriteria<D> query(Map params) {
        query(QueryArgs.of(params), null)
    }

    // default GormStaticApi<D> gormStaticApi() {
    //     (GormStaticApi<D>) GormEnhancer.findStaticApi(getEntityClass())
    // }
}
