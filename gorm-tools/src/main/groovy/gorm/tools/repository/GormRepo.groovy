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
import org.grails.datastore.mapping.transactions.TransactionObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver
import org.springframework.dao.DataAccessException
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionStatus

import gorm.tools.databinding.BindAction
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.mango.api.QueryMangoEntityApi
import gorm.tools.model.Lookupable
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.repository.errors.RepoEntityErrors
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.model.DataOp
import grails.validation.ValidationException
import yakworks.commons.lang.ClassUtils

/**
 * A trait that turns a class into a Repository
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@CompileStatic
trait GormRepo<D> implements RepoEntityErrors<D>, QueryMangoEntityApi<D> {

    @Autowired EntityMapBinder entityMapBinder

    @Autowired RepoEventPublisher repoEventPublisher

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
    D persist(D entity, Map args = [:]) {
        entityTrx {
            doPersist(entity, args)
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
    D doPersist(D entity, Map args) {
        try {
            args['failOnError'] = args.containsKey('failOnError') ? args['failOnError'] : true
            getRepoEventPublisher().doBeforePersist(this, (GormEntity)entity, args)
            gormInstanceApi().save entity, args
            // entity.save()
            getRepoEventPublisher().doAfterPersist(this, (GormEntity)entity, args)
            return entity
        }
        catch (ValidationException | DataAccessException ex) {
            throw RepoExceptionSupport.translateException(ex, entity)
        }
    }

    /**
     * Transactional wrap for {@link #doCreate}
     */
    D create(Map data, Map args = [:]) {
        entityTrx {
            doCreate(data, args)
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
    D doCreate(Map data, Map args) {
        D entity = (D) getEntityClass().newInstance()
        bindAndCreate(entity, data, args)
        return entity
    }

    /**
     * just calls bindAndSave with the right BindAction
     * This is the best method to overrride if needed
     */
    void bindAndCreate(D entity, Map data, Map args) {
        bindAndSave(entity, data, BindAction.Create, args)
    }

    /**
     * Transactional wrap for {@link #doUpdate}
     */
    D update(Map data, Map args = [:]) {
        entityTrx {
            doUpdate(data, args)
        }
    }

    /**
     * Updates entity using the data from params. calls the {@link #bind} with bindMethod='Update'
     *
     * @param data the data to bind onto the entity
     * @return the updated domain entity
     * @see #doPersist
     */
    D doUpdate(Map data, Map args) {
        //FIXME #339 I think we do the lookup logic here, framed sample out sample below
        D entity = findWithData(data)
        bindAndUpdate(entity, data, args)
        return entity
    }

    /**
     * Uses the items in the data to find the entity.
     * If data has an id key the use that.
     * otherwise it will see if entity has lookupable or if the concrete repo has a lookup method
     *
     * @param data - the map with the keys for lookup
     * @param boolean mustExist - if the record must exist, default true
     * @return the found entity
     * @throws EntityNotFoundException if mustExist is true, and entity not found
     */
    D findWithData(Map data, boolean mustExist = true) {
        D foundEntity
        def ident = data['id'] as Serializable
        //check by id first
        if(ident){
            //return it fast if its good to go, will have blown and error if not found
            return get(ident, data['version'] as Long)
        } else if(Lookupable.isAssignableFrom(getEntityClass())){
            // call the lookup if domain implements the Lookupable
            //FIXME is there a cleaner way to do this?
            foundEntity = (D) ClassUtils.callStaticMethod(getEntityClass(), 'lookup', data)
        } else {
            foundEntity = lookup(data)
        }
        if(mustExist) RepoUtil.checkFound(foundEntity, 'data map', getEntityClass().name)
        return foundEntity
    }

    /**
     * does nothing, can be implemented by the conrete repo for special lookup logic such as for souceId
     */
    D lookup(Map data) {
        return null
    }

    void bindAndUpdate(D entity, Map data, Map args) {
        bindAndSave(entity, data, BindAction.Update, args)
    }

    /** short cut to call {@link #bind}, setup args for events then calls {@link #doPersist} */
    void bindAndSave(D entity, Map data, BindAction bindAction, Map args){
        args['bindAction'] = bindAction
        bind(entity, data, bindAction, args)
        //set the id if it has one in data and bindId arg is passed in as true
        if(args.remove('bindId') && BindAction.Create == bindAction && data['id']) entity['id'] = data['id']
        args['data'] = data
        doPersist(entity, args)
    }

    /**
     * binds by calling {@link #doBind} and fires before and after events
     * better to override doBind in implementing classes for custom binding logic.
     * Or even better implement the beforeBind|afterBind event methods
     */
    void bind(D entity, Map data, BindAction bindAction, Map args = [:]) {
        getRepoEventPublisher().doBeforeBind(this, (GormEntity)entity, data, bindAction, args)
        doBind(entity, data, bindAction, args)
        getRepoEventPublisher().doAfterBind(this, (GormEntity)entity, data, bindAction, args)
    }

    /**
     * Main bind method that redirects call to the injected mapBinder.
     * override this one in implementing classes.
     * can also call this if you do NOT want the before/after Bind events to fire
     */
    void doBind(D entity, Map data, BindAction bindAction, Map args) {
        getEntityMapBinder().bind(args, entity, data)
    }

    /**
     * Remove by ID
     *
     * @param id - the id to delete
     * @param args - the args to pass to delete. flush being the most common
     *
     * @throws EntityNotFoundException if its not found or if a DataIntegrityViolationException is thrown
     */
    void removeById(Serializable id, Map args = [:]) {
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
    void remove(D entity, Map args = [:]) {
        gormStaticApi().withTransaction {
            doRemove(entity, args)
        }
    }

    /**
     * no trx wrapper. delete entity.
     *
     * @param entity - the domain instance to delete
     * @param args - args passed to delete
     */
    void doRemove(D entity, Map args = [:]) {
        try {
            getRepoEventPublisher().doBeforeRemove(this, (GormEntity)entity, args)
            gormInstanceApi().delete(entity, args)
            getRepoEventPublisher().doAfterRemove(this, (GormEntity)entity, args)
        }
        catch (DataAccessException ex) {
            throw RepoExceptionSupport.translateException(ex, entity)
        }
    }

    /**
     * creates, removes or updates the entity based on DataOp
     * if data has an id then its considered an update
     * if data has an id and data.op == remove then it will delete it, see DataOp enum
     * otherwise create it
     * XXX needs tests
     */
    //FIXME #339 if dont like this method and think it should be baked.
    // this does nothing special for create so it really just for the update and deletes
    // if we want to support multiple ways to do look ups, for code for example, then this wont work
    // we can put the special look up logic in the main update maybe.
    D createOrUpdate(Map data, Map args = [:]) {
        if (!data) return
        D instance = findWithData(data, false)
        if (instance) {
            instance = update(data, args)
        } else {
            instance = create(data, args)
        }
        return instance
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
    D get(Serializable id, Long version) {
        D entity = get(id)
        RepoUtil.checkFound(entity, id, getEntityClass().name)
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
        entityTrx {
            doGet(id)
        }
    }

    D doGet(Serializable id) {
        (D)gormStaticApi().get(id)
    }

    /**
     * a read wrapped in a read-only transaction.
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D read(Serializable id) {
        entityReadOnlyTrx {
            (D) gormStaticApi().read(id)
        }
    }

    /**
     * a read wrapped in a read-only transaction.
     *
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D load(Serializable id) {
        (D) gormStaticApi().load(id)
    }

    /**
     * batchCreateOrUpdate associations for given entity
     *
     * @Param mainEntity The entity that has the associations that are being created/updated
     * @param assocRepo association entity repo
     * @param assocList the list of data maps to create/update
     * @param belongsToProp the name of parent property to set, if any
     * @return the list of created entities
     */
    List persistAssociationData(D entity, GormRepo assocRepo, List<Map> assocList, String belongsToProp = null){
        if(belongsToProp) assocList.each { it[belongsToProp] = entity}
        assocRepo.batchCreateOrUpdate(assocList)
    }

    /**
     * Mass update a list of ids
     *
     * @param ids list of ids to update
     * @param values data to apply to selected rows
     * @return the converted list of maps that was used to update
     */
    //FIXME #339 we have to many bulks and batch. centralize and/or fix names
    List<Map> bulkUpdate(List ids, Map values){
        List<Map> data = ids.collect {
            values.id = it
            values
        } as List<Map>

        batchTrx(data) { Map item ->
            doUpdate(item, [:])
        }
        data
    }

    /**
     * batch creates or updates a list of items in a trx
     * Will rollback on any error
     *
     * @param dataList the list of data maps to create/update
     * @return the list of created entities
     */
    //FIXME #339 lets move to Bulkable?
    //@KJosh, in use cases such as doAssociations, when doing one-to-many collection handling,
    // we just want to insert list of contacts for an org etc, batchCreateOrUpdate makes it easier, without needing its repo to be Bulkable.
    //We use it for persistAssociationData when doing associations
    List<D> batchCreateOrUpdate(List<Map> dataList){
        List resultList = [] as List<D>

        batchTrx(dataList) { Map item ->
            resultList << createOrUpdate(item)
        }

        return resultList
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

    void flushAndClear() {
        flush()
        clear()
    }

    /**
     * modification of gorm's default to make it more like the @Transactional annotation which uses CustomizableRollbackTransactionAttribute
     * Specifically for wrapping something that will return the domain entity for this (such as a get, create or update)
     * like withTransaction this creates one if none is present or joins an existing transaction if one is already present.
     *
     * @param callable The closure to call
     * @return The entity that was run in the closure
     */
    D entityTrx(Closure<D> callable) {
        def trxAttr = new CustomizableRollbackTransactionAttribute()
        gormStaticApi().withTransaction(trxAttr, callable)
    }

    public <T> T withTrx(Closure<T> callable) {
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
    D entityReadOnlyTrx(Closure<D> callable) {
        def trxAttr = new CustomizableRollbackTransactionAttribute()
        trxAttr.readOnly = true
        gormStaticApi().withTransaction(trxAttr, callable)
    }

    void flushAndClear(TransactionStatus status) {
        status.flush()
        clear(status)
    }

    //@CompileDynamic
    void clear(TransactionStatus status) {
        TransactionObject txObject = (status as DefaultTransactionStatus).transaction as TransactionObject
        txObject.sessionHolder.getSession().clear()
    }

    /* -- batch methods -- */

    /**
     * Transactional, Iterates over list and runs closure for each item
     */
    //FIXME #339 move these out with bulkCreateOrUpdate? these are only used in benchmarks they needed?
    //@josh we use this for association handling etc, when we want to store list of contacts for an org after persist
    //one to many associations needs simple loop to create/update associations in a batch, when doing bindAssociation,
    // and doesnt need Job or any other bulk functionality, so may be we can keep it here?
    void batchTrx(List list, Closure closure) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Object item : list) {
                closure(item)
            }
            flushAndClear()
        }
    }

    void batchCreate(Map args = [:], List<Map> list) {
        batchTrx(list) { Map item ->
            doCreate(item, args)
        }
    }

    GormInstanceApi<D> gormInstanceApi() {
        (GormInstanceApi<D>)GormEnhancer.findInstanceApi(getEntityClass())
    }

    GormStaticApi<D> gormStaticApi() {
        (GormStaticApi<D>)GormEnhancer.findStaticApi(getEntityClass())
    }

    GormValidationApi gormValidationApi() {
        GormEnhancer.findValidationApi(getEntityClass())
    }

}
