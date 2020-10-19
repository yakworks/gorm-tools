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
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.transactions.CustomizableRollbackTransactionAttribute
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.GenericTypeResolver
import org.springframework.dao.DataAccessException

import gorm.tools.databinding.BindAction
import gorm.tools.databinding.MapBinder
import gorm.tools.mango.api.QueryMangoEntityApi
import gorm.tools.repository.api.GormBatchRepo
import gorm.tools.repository.api.RepositoryApi
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import grails.validation.ValidationException

/**
 * A trait that turns a class into a Repository
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@CompileStatic
trait GormRepo<D> implements GormBatchRepo<D>, QueryMangoEntityApi<D>, RepositoryApi<D> {

    @Qualifier("entityMapBinder")
    @Autowired MapBinder mapBinder

    @Autowired RepoEventPublisher repoEventPublisher
    @Autowired RepoExceptionSupport repoExceptionSupport

    /** default to true. If false only method events are invoked on the implemented Repository. */
    Boolean enableEvents = true

    /**
     * The java class for the Gorm domain (persistence entity). will generally get set in constructor or using the generic as
     * done in {@link gorm.tools.repository.GormRepo#getEntityClass}
     * using the {@link org.springframework.core.GenericTypeResolver}
     * @see org.grails.datastore.mapping.model.PersistentEntity#getJavaClass().
     */
    Class<D> entityClass // the domain class this is for

    /**
     * The gorm domain class. uses the {@link org.springframework.core.GenericTypeResolver} is not set during contruction
     */
    @Override
    Class<D> getEntityClass() {
        if (!entityClass) this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), GormRepo)
        return entityClass
    }

    /**
     * Transactional wrap for {@link #doPersist}
     * Saves a domain entity with the passed in args and rewraps ValidationException with EntityValidationException on error.
     *
     * @param entity the domain entity to call save on
     * @param args the arguments to pass to save. adds the following but see doPersist for more.
     *   - tx: defaults to true. set to true to make sure this is wrapped in a transaction.
     * @throws DataAccessException if a validation or DataAccessException error happens
     */
    @Override
    D persist(Map args = [:], D entity) {
        withTrx {
            doPersist(args, entity)
        }
        return entity
    }

    /**
     * saves a domain entity with the passed in args. Not wrapped in a transaction.
     * If a {@link ValidationException} is caught it wraps and throws it with our DataValidationException.
     *
     * @param entity the domain entity to call save on
     * @param args (optional) - the arguments to pass to save as well as the PersistEvents.  can be any of the normal gorm save args
     * plus some others specific to here
     *   - failOnError: (boolean) defaults to true
     *   - flush: (boolean) flush the session
     *   - bindType: (String) "Create" or "Update" when coming from those actions/methods
     *   - data: (Map) if it was a Create or Update method called then this is the data and gets passed into events
     *
     * @throws DataAccessException if a validation or DataAccessException error happens
     */
    @Override
    D doPersist(Map args = [:], D entity) {
        try {
            args['failOnError'] = args.containsKey('failOnError') ? args['failOnError'] : true
            getRepoEventPublisher().doBeforePersist(this, (GormEntity)entity, args)
            gormInstanceApi().save entity, args
            // entity.save()
            getRepoEventPublisher().doAfterPersist(this, (GormEntity)entity, args)
            return entity
        }
        catch (ValidationException | DataAccessException ex) {
            throw handleException(ex, entity)
        }
    }

    /**
     * Transactional wrap for {@link #doCreate}
     */
    @Override
    D create(Map args = [:], Map data) {
        withTrx {
            doCreate(args, data)
        }
    }

    /**
     * Creates entity using the data from params. Not wrapped in a transaction.
     * calls the {@link #bind} with bindMethod='Create'
     *
     * @param args the variable args that would get passed to save
     * @param data the data to bind onto the entity
     * @return the created domain entity
     * @see #doPersist
     */
    @Override
    D doCreate(Map args, Map data) {
        D entity = (D) getEntityClass().newInstance()
        bindAndSave(args, entity, data, BindAction.Create)
        return entity
    }

    /**
     * Transactional wrap for {@link #doUpdate}
     */
    @Override
    D update(Map args = [:], Map data) {
        withTrx {
            doUpdate(args, data)
        }
    }

    /**
     * Updates entity using the data from params. calls the {@link #bind} with bindMethod='Update'
     *
     * @param data the data to bind onto the entity
     * @return the updated domain entity
     * @see #doPersist
     */
    @Override
    D doUpdate(Map args, Map data) {
        D entity = get(data['id'] as Serializable, data['version'] as Long)
        bindAndSave(args, entity, data, BindAction.Update)
        return entity
    }

    /** short cut to call {@link #bind}, setup args for events then calls {@link #doPersist} */
    void bindAndSave(Map args, D entity, Map data, BindAction bindAction){
        args['bindAction'] = bindAction
        bind(args, entity, data, bindAction)
        //set the id if it has one in data and bindId arg is passed in as true
        if(args.remove('bindId') && BindAction.Create == bindAction && data['id']) entity['id'] = data['id']
        args['data'] = data
        doPersist(args, entity)
    }

    /**
     * binds by calling {@link #doBind} and fires before and after events
     * better to override doBind in implementing classes for custom binding logic.
     * Or even better implement the beforeBind|afterBind event methods
     */
    @Override
    void bind(Map args = [:], D entity, Map data, BindAction bindAction) {
        getRepoEventPublisher().doBeforeBind(this, (GormEntity)entity, data, bindAction, args)
        doBind(args, entity, data, bindAction)
        getRepoEventPublisher().doAfterBind(this, (GormEntity)entity, data, bindAction, args)
    }

    /**
     * Main bind method that redirects call to the injected mapBinder.
     * override this one in implementing classes.
     * can also call this if you do NOT want the before/after Bind events to fire
     */
    @Override
    void doBind(Map args, D entity, Map data, BindAction bindAction) {
        getMapBinder().bind(args, entity, data)
    }

    /**
     * Remove by ID
     *
     * @param id - the id to delete
     * @param args - the args to pass to delete. flush being the most common
     *
     * @throws EntityNotFoundException if its not found or if a DataIntegrityViolationException is thrown
     */
    @Override
    void removeById( Map args = [:], Serializable id) {
        gormStaticApi().withTransaction {
            D entity = get(id, null)
            doRemove(entity)
        }
    }

    /**
     * Transactional, Calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions.
     *
     * @param entity the domain entity
     * @throws EntityValidationException if a spring DataIntegrityViolationException is thrown
     */
    @Override
    void remove(Map args = [:], D entity) {
        gormStaticApi().withTransaction {
            doRemove(args, entity)
        }
    }

    /**
     * no trx wrapper. delete entity.
     *
     * @param entity - the domain instance to delete
     * @param args - args passed to delete
     */
    void doRemove(Map args = [:], D entity) {
        try {
            getRepoEventPublisher().doBeforeRemove(this, (GormEntity)entity, args)
            gormInstanceApi().delete(entity, args)
            getRepoEventPublisher().doAfterRemove(this, (GormEntity)entity, args)
        }
        catch (DataAccessException ex) {
            throw handleException(ex, entity)
        }
    }

    /**
     * gets and verifies that the entity can be retrieved and version matches throwing error if not.
     *
     * @param id required, the id to get
     * @param version - can be null. if its passed in then it validates its that same as the version in the retrieved entity.
     * @return the retrieved entity. Will always be an entity as this throws an error if not
     *
     * @throws EntityNotFoundException if its not found
     * @throws gorm.tools.repository.errors.EntityValidationException if the versions mismatch
     */
    @Override
    D get(Serializable id, Long version) {
        D entity = get(id)
        RepoUtil.checkFound(entity, [id: id], getEntityClass().name)
        if (version != null) RepoUtil.checkVersion(entity, version)
        return entity
    }

    /**
     * a get wrapped in a transaction.
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D get(Serializable id) {
        withTrx {
            (D) gormStaticApi().get(id)
        }
    }

    /**
     * a read wrapped in a read-only transaction.
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D read(Serializable id) {
        withReadOnlyTrx {
            (D) gormStaticApi().read(id)
        }
    }

    void publishBeforeValidate(Object entity) {
        getRepoEventPublisher().doBeforeValidate(this, entity, [:])
    }

    @Override
    RuntimeException handleException(RuntimeException ex, D entity) {
        return getRepoExceptionSupport().translateException(ex, entity)
    }

    /** gets the datastore for this Gorm domain instance */
    Datastore getDatastore() {
        gormInstanceApi().datastore
    }

    /** flush on the datastore's currentSession. When possible use the transactionStatus.flush(). see WithTrx trait */
    void flush(){
        getDatastore().currentSession.flush()
    }

    /** cache clear on the datastore's currentSession. When possible use the transactionStatus. see WithTrx trait  */
    void clear(){
        getDatastore().currentSession.clear()
    }

    /**
     * modification of gorm's default to make it more like the @Transactional annotation which uses CustomizableRollbackTransactionAttribute
     * Specifically for wrapping something that will return the domain entity for this (such as a get, create or update)
     * like withTransaction this creates one if none is present or joins an existing transaction if one is already present.
     *
     * @param callable The closure to call
     * @return The entity that was run in the closure
     */
    D withTrx(Closure<D> callable) {
        def trxAttr = new CustomizableRollbackTransactionAttribute()
        gormStaticApi().withTransaction(trxAttr, callable)
    }

    /**
     * Read-only specifically for wrapping something that will return the domain entity for this (such as a get or read)
     * like withTransaction this creates one if none is present or joins an existing transaction if one is already present.
     *
     * @param callable The closure to call
     * @return The entity that was run in the closure
     */
    D withReadOnlyTrx(Closure<D> callable) {
        def trxAttr = new CustomizableRollbackTransactionAttribute()
        trxAttr.readOnly = true
        gormStaticApi().withTransaction(trxAttr, callable)
    }

    GormInstanceApi<D> gormInstanceApi() {
        (GormInstanceApi<D>)GormEnhancer.findInstanceApi(getEntityClass())
    }

    GormStaticApi<D> gormStaticApi() {
        (GormStaticApi<D>)GormEnhancer.findStaticApi(getEntityClass())
    }
}
