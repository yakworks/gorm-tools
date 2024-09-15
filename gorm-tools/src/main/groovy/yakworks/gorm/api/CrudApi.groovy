/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api

import groovy.transform.CompileStatic

import gorm.tools.beans.Pager
import gorm.tools.job.SyncJobEntity
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

    //MangoQuery getMangoQuery()

    /**
     * Create entity from data and return the MetaMap of what was created
     */
    Map create(Map data, Map params)

    /**
     * Update entity from data
     */
    Map update(Map data, Map params)

    /**
     * Create or Update entity from data. Checks if key exists and updates, otherwise inserts
     */
    Map upsert(Map data, Map params)

    /**
     * Remove by ID
     * @param id - the id to delete
     * @param args - the PersistArgs to pass to delete. flush being the most common
     *
     * @throws NotFoundProblem.Exception if its not found or DataProblemException if a DataIntegrityViolationException is thrown
     */
    void removeById(Serializable id, Map params)

    /**
     * gets the entity by id
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    Map get(Serializable id, Map params)

    /**
     * Checks if the id exists
     */
    boolean exists(Serializable id)

    /**
     * Mango Query that returns a paged list
     */
    Pager list(Map params, List<String> includesKeys)

    SyncJobEntity bulk(DataOp dataOp, List<Map> dataList, Map params, String sourceId)
}
