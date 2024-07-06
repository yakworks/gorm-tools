/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.gorm.GormInstanceApi
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.gorm.GormValidationApi
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.transactions.CustomizableRollbackTransactionAttribute
import org.springframework.dao.DataAccessException
import org.springframework.transaction.TransactionDefinition

import gorm.tools.model.Persistable
import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.transaction.TrxUtils
import gorm.tools.utils.GormMetaUtils
import grails.validation.ValidationException
import yakworks.api.problem.ThrowableProblem
import yakworks.api.problem.data.NotFoundProblem

/**
 * A trait that turns a class into a Repository
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@SuppressWarnings(['EmptyMethod'])
@CompileStatic
interface IGormRepo<D> {

    Class<D> getEntityClass()

    /**
     * Transactional wrap for {@link #doPersist}
     * Saves a domain entity with the passed in args and rewraps ValidationException with EntityValidationException on error.
     *
     * @param entity the domain entity to call save on
     * @param args the arguments to pass to save.
     * @throws DataAccessException if a validation or DataAccessException error happens
     */
    D persist(D entity, PersistArgs args)

    D persist(D entity, Map args)

    /**
     * saves a domain entity with the passed in args. Not wrapped in a transaction.
     * If a {@link ValidationException} is caught it wraps and throws it with our DataValidationException.
     * Don' Override this, use the other events or event methods
     *
     * @param entity the domain entity to call save on
     * @param args will be passed through to support methods as well as the events
     *
     * @throws DataAccessException if a validation or DataAccessException error happens
     * @throws ValidationProblem.Exception if a validation fails
     */
    D doPersist(D entity, PersistArgs args)

    /**
     * validates the entity and throws a ValidationProblem if it fails
     */
    boolean validate(D entity, PersistArgs args)

    /**
     * Transactional wrap for {@link #doCreate}
     */
    D create(Map data, PersistArgs args)

    D create(Map data, Map args)

    /**
     * Transactional wrap for {@link #doUpdate}
     */
    default D update(Map data, Map args) {
        doUpdate(data, PersistArgs.of(args))
    }

    /**
     * Updates entity using the data from params. calls the {@link #bind} with bindMethod='Update'
     *
     * @param data the data to bind onto the entity
     * @return the updated domain entity
     * @see #doPersist
     */
    D doUpdate(Map data, PersistArgs args)


    /**
     * Uses the items in the data to find the entity.
     * If data has an id key the use that.
     * otherwise it will see if entity has lookupable or if the concrete repo has a lookup method
     *
     * @param data - the map with the keys for lookup
     * @param boolean mustExist - if the record must exist, default true
     * @return the found entity
     * @throws NotFoundProblem.Exception if mustExist is true, and entity not found
     */
    D findWithData(Map data, boolean mustExist)


    /**
     * Remove by ID
     *
     * @param id - the id to delete
     * @param args - the args to pass to delete. flush being the most common
     *
     * @throws NotFoundProblem.Exception if its not found or DataProblemException if a DataIntegrityViolationException is thrown
     */
    void removeById(Serializable id, Map args)

    void removeById(Serializable id)

    /**
     * Transactional, Calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions.
     *
     * @param entity the domain entity
     * @throws ThrowableProblem if a DataIntegrityViolationException is thrown
     */
    void remove(D entity)
    void remove(D entity, Map args)

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
    D get(Serializable id, Long version)

    /**
     * simple call to the gormStaticApi get, not in a trx to avoid overhead
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D get(Serializable id)

    /**
     * wraps get in a trx. NOT a read only trx like read as that messes with dirty tracking
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D getWithTrx(Serializable id)


    /**
     * a read wrapped in a read-only transaction.
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D read(Serializable id)

    /**
     * a read wrapped in a read-only transaction.
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D load(Serializable id)

    /**
     * Creates or updates a list of items.
     * Use for small datasets, use bulk operations for larger datasets.
     * iterates and just calls createOrUpdateItem, no special handling for data.op
     * NOT Transactional, so should be wrapped in a trx to function properly.
     * Will throw exception on any error.
     *
     * @param dataList the list of data maps to create/update
     * @return the list of created or updated entities
     */
    List<D> createOrUpdate(List<Map> dataList)

    /**
     * Simple helper to updating from data.
     * Uses findWithData and if instance is found then considers it an update.
     * If not found then considers it a create.
     * DOES NOT do anything with data.op operations.
     * NOT transactional so should be wrapped in a transaction.
     */
    D createOrUpdateItem(Map data, PersistArgs args)

    /** gets the datastore for this Gorm domain instance */
    default Datastore getDatastore() {
        gormInstanceApi().datastore
    }

    /** flush on the datastore's currentSession.*/
    default void flush(){
        TrxUtils.flush(getDatastore())
    }

    /** cache clear on the datastore's currentSession.*/
    default void clear(){
        TrxUtils.clear(getDatastore())
    }

    default void flushAndClear() {
        try {
            flush()
        } finally {
            //clear, even if flush failed
            clear()
        }
    }

    /**
     * Helper method to check if item is new. Checks in this order <br>
     * 1. PersistArgs.insert = true
     * 2. entity.isNew()
     */
    default boolean isNew(Persistable entity, PersistArgs args) {
        args.insert || entity.isNew()
    }

    /**
     * checks if its new or if its dirty
     */
    default boolean isNewOrDirty(GormEntity entity) {
        GormMetaUtils.isNewOrDirty(entity)
    }

    /**
     * modification of gorm's default to make it more like the @Transactional annotation which uses CustomizableRollbackTransactionAttribute
     * Specifically for wrapping something that will return the domain entity for this (such as a get, create or update)
     * like withTransaction this creates one if none is present or joins an existing transaction if one is already present.
     *
     * @param callable The closure to call
     * @return The entity that was run in the closure or the generic on the closure if otherwise
     */
    default public <T> T withTrx(Closure<T> callable) {
        def trxAttr = new CustomizableRollbackTransactionAttribute()
        withTrx(trxAttr, callable)
    }

    default public <T> T withTrx(TransactionDefinition trxAttr, Closure<T> callable) {
        try {
            gormStaticApi().withTransaction(trxAttr, callable)
        } catch(ValidationException | DataAccessException ex) {
            //Many of the exceptions such as PK/FK constraint failures, Optimistic locking failures will happen when transaction commits
            throw RepoExceptionSupport.translateException(ex, null)
        }
    }

    default public <T> T withNewTrx(Closure<T> callable) {
        def trxAttr = new CustomizableRollbackTransactionAttribute()
        trxAttr.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        withTrx(trxAttr, callable)
    }

    /**
     * Read-only specifically for wrapping something that will return the domain entity for this (such as a get or read)
     * like withTransaction this creates one if none is present or joins an existing transaction if one is already present.
     *
     * @param callable The closure to call
     * @return The entity that was run in the closure
     */
    default public <T> T withReadOnlyTrx(Closure<T> callable) {
        def trxAttr = new CustomizableRollbackTransactionAttribute()
        trxAttr.readOnly = true
        gormStaticApi().withTransaction(trxAttr, callable)
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

}
