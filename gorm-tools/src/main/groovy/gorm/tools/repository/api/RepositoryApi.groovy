/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.api

import org.springframework.dao.DataAccessException

import gorm.tools.databinding.BindAction
import gorm.tools.databinding.MapBinder
import grails.validation.ValidationException

/**
 * A complete interface for the Repository.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@SuppressWarnings("MethodCount")
interface RepositoryApi<D> {

    /**
     * The java class for the Gorm domain (persistence entity). will generally get set in constructor or using the generic as
     * done in {@link gorm.tools.repository.GormRepo#getEntityClass}
     * using the {@link org.springframework.core.GenericTypeResolver}
     * @see org.grails.datastore.mapping.model.PersistentEntity#getJavaClass().
     */
    Class<D> getEntityClass()

    /** The data binder to use. By default gets injected with EntityMapBinder*/
    MapBinder getMapBinder()

    /** default to true. If false only method events are invoked on the implemented Repository. */
    Boolean getEnableEvents()

    /**
     * Transactional wrap for {@link #doPersist} with arguments to pass to save
     */
    D persist(D entity, Map args)

    /**
     * Transactional wrap for {@link #doPersist}
     */
    D persist(D entity)

    /**
     * saves a domain entity with the passed in args.
     * If a {@link ValidationException} is caught it wraps and throws it with our DataValidationException.
     *
     * @param entity the domain entity to call save on
     * @param args the arguments to pass to save
     * @throws DataAccessException if a validation or DataAccessException error happens
     */
    D doPersist(D entity, Map args)

    /**
     * Transactional wrap for {@link #doCreate}
     */
    D create(Map data)
    D create(Map data, Map args)

    /**
     * Creates entity using the data from params. calls the {@link #bind} with BindAction.Create
     *
     * @param data the data to bind onto the entity
     * @return the created domain entity
     * @see #doPersist
     */
    D doCreate(Map data, Map args)

    /**
     * Transactional wrap for {@link #doUpdate}
     */
    D update(Map data)
    D update(Map args, Map data)

    /**
     * Updates entity using the data from params. calls the {@link #bind} with BindAction.Update
     *
     * @param data the data to bind onto the entity
     * @return the updated domain entity
     * @see #doPersist
     */
    D doUpdate(Map data, Map args)

    /**
     * binds by calling {@link #doBind} and fires before and after events
     * better to override doBind in implementing classes for custom logic.
     * Or just implement the beforeBind|afterBind event methods
     */
    void bind(Map args, D entity, Map data, BindAction bindAction)

    /**
     * Main bind method that redirects call to the injected mapBinder. see {@link #getMapBinder}
     * override this one in implementing classes. can also call this if you don't want events to fire
     */
    void doBind(Map args, D entity, Map data, BindAction bindAction)

    /**
     * Remove by ID. Wrapping this in a Transaction in an implementing class here is optional
     *
     * @param id - the id for the
     * @param args - the args to pass to delete. can be null and [flush] being the most common
     *
     * @throws gorm.tools.repository.errors.EntityValidationException if its not found or if a DataIntegrityViolationException is thrown
     */
    void removeById(Serializable id, Map args)
    void removeById(Serializable id)

    /**
     * Wrappep in Trx. Calls doRemove.
     *
     * @param entity the domain entity
     */
    void remove(D entity)
    void remove(D entity, Map args)

    void doRemove(D entity, Map args)
    void doRemove(D entity)

    /**
     * gets and verifies that the entity can be retrieved and version matches.
     *
     * @param id required, the id to get
     * @param version - can be null. if its passed in then it validates its that same as the version in the retrieved entity.
     * @return the retrieved entity. Will always be an entity as this throws an error if not
     *
     * @throws gorm.tools.repository.errors.EntityNotFoundException if its not found
     * @throws org.springframework.dao.OptimisticLockingFailureException if the versions mismatch
     */
    D get(Serializable id, Long version)

    /**
     * a get wrapped in a transaction.
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D get(Serializable id)

    RuntimeException handleException(RuntimeException e, D entity)

    void batchCreate(Map args, List<Map> batch)

    void batchCreate(List<Map> batch)

    void batchUpdate(Map args, List<Map> batch)

    void batchUpdate(List<Map> batch)

    void batchRemove(Map args, List batch)

    void batchRemove(List batch)

    void batchPersist(Map args, List<D> list)

    void batchPersist(List<D> list)
}
