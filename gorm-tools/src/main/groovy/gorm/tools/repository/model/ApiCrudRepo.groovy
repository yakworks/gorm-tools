/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormInstanceApi
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.gorm.GormValidationApi

import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.RepoUtil
import yakworks.api.problem.data.NotFoundProblem

/**
 * CRUD api for rest repo
 */
@CompileStatic
interface ApiCrudRepo<D> {

    Class<D> getEntityClass()

    /**
     * Transactional wrap for {@link #doCreate}
     */
    D create(Map data, PersistArgs args)

    default D create(Map data) {
        create(data, PersistArgs.of())
    }

    /**
     * Transactional wrap for {@link #doUpdate}
     */
    D update(Map data, PersistArgs args)

    default D update(Map data) {
        update(data, PersistArgs.defaults())
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
    default D get(Serializable id) {
        (D)gormStaticApi().get(id)
    }

    /**
     * read only get
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    default D read(Serializable id) {
        (D)gormStaticApi().read(id)
    }

    /**
     * load without hydrating.
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    default D load(Serializable id) {
        (D) gormStaticApi().load(id)
    }

    default GormInstanceApi<D> gormInstanceApi() {
        (GormInstanceApi<D>)GormEnhancer.findInstanceApi(getEntityClass())
    }

    default GormStaticApi<D> gormStaticApi() {
        (GormStaticApi<D>)GormEnhancer.findStaticApi(getEntityClass())
    }

    default GormValidationApi gormValidationApi() {
        GormEnhancer.findValidationApi(getEntityClass())
    }

    boolean exists(Serializable id)

}
