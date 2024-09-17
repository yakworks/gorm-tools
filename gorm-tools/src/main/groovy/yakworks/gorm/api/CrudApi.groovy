/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api

import groovy.transform.CompileStatic

import gorm.tools.beans.Pager
import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.model.ApiCrudRepo
import gorm.tools.repository.model.DataOp
import yakworks.api.problem.data.NotFoundProblem

/**
 * CRUD api for rest repo
 */
@CompileStatic
interface CrudApi<D> {

    Class<D> getEntityClass()

    ApiCrudRepo<D> getApiCrudRepo()

    /**
     * Wrapper/Holder for the result. Allows to have entity reference and chain method to do asMap
     */
    interface EntityResult<D>{
        IncludesProps getIncludesProps()
        D getEntity()
        Map asMap()
    }

    /**
     * gets the entity by id
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    EntityResult<D> get(Serializable id, Map params)

    /**
     * Create entity from data and return the MetaMap of what was created
     */
    EntityResult<D> create(Map data, Map params)

    /**
     * Update entity from data
     */
    EntityResult<D> update(Map data, Map params)

    /**
     * Create or Update entity from data. Checks if key exists and updates, otherwise inserts
     */
    EntityResult<D> upsert(Map data, Map params)

    /**
     * Remove by ID
     * @param id - the id to delete
     * @param args - the PersistArgs to pass to delete. flush being the most common
     *
     * @throws NotFoundProblem.Exception if its not found or DataProblemException if a DataIntegrityViolationException is thrown
     */
    void removeById(Serializable id, Map params)

    /**
     * Checks if the id exists
     * NOTE: Not used yet
     */
    boolean exists(Serializable id)

    /**
     * Mango Query that returns a paged list
     *
     * @param params: the query params
     * @param includesKeys: the default fallback includesKeys as params will normally not be passed in
     * @return the Pager with the data populated with the requested page
     */
    Pager list(Map params, List<String> includesKeys)

    /**
     * bulk operations on entity
     *
     * @param dataOp: the data operation
     * @param dataList: the dataList to bulk update or insert
     * @param params: query string params. Will be used to build SyncJobArgs
     * @param sourceId: sourceId for the Job, controller uses JobUtils.requestToSourceId to set it.
     * @return the created SyncJobEntity, normally async=true and syncJob.state will be 'Running'
     */
    SyncJobEntity bulk(DataOp dataOp, List<Map> dataList, Map params, String sourceId)

    /**
     * Converts the instance to Map using the MetaMap wrapper with {@link gorm.tools.metamap.services.MetaMapService}.
     *
     * @param instance the entity instance
     * @param includesProps the includes list or keys to use to generate the meta map
     * @return the MetaMap that can be converted to json
     */
    Map entityToMap(D instance, IncludesProps incProps)

    /**
     * Creates the EntityResult.
     * This is called bu crud methods and makes it easy to override to return custom implementation
     */
    EntityResult<D> createEntityResult(D instance, Map qParams)
}