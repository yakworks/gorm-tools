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
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionStatus

import gorm.tools.databinding.BindAction
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.mango.api.QueryMangoEntityApi
import gorm.tools.model.Lookupable
import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.bulk.BulkableRepo
import gorm.tools.repository.errors.RepoEntityErrors
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.model.PersistableRepoEntity
import gorm.tools.transaction.TrxService
import grails.validation.ValidationException
import yakworks.commons.lang.ClassUtils
import yakworks.commons.map.Maps
import yakworks.problem.data.NotFoundProblem

/**
 * A trait that turns a class into a Repository
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@CompileStatic
trait GormRepo<D> implements BulkableRepo<D>, RepoEntityErrors<D>, QueryMangoEntityApi<D> {

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
     * @throws ValidationProblem.Exception if a validation fails
     */
    D doPersist(D entity, Map args) {
        try {
            //set failOnError to true if not set
            args.put('failOnError' , Maps.getBoolean('failOnError', args, true))

            getRepoEventPublisher().doBeforePersist(this, (GormEntity)entity, args)

            validate(entity, args)
            args.put('validate', false) //set it false so we dont do it again

            persistAssociations(entity, args)

            gormInstanceApi().save entity, args

            getRepoEventPublisher().doAfterPersist(this, (GormEntity)entity, args)
            return entity
        }
        catch (ValidationException | DataAccessException ex) {
            throw RepoExceptionSupport.translateException(ex, entity)
        }
    }

    /**
     * called from doPersist after validate as opposed to doBeforePersist which is called before.
     * This can be overriden but defaults to using the toOneAssociations property to pre-persist
     */
    void persistAssociations(D entity, Map args){
        //so it doesn't do extra select, save associations first
        if(getToOneAssociations()) persistToOneAssociations(entity, getToOneAssociations())
    }

    /**
     * validates the entity and throws a ValidationProblem if it fails
     */
    boolean validate(D entity, Map args) {
        boolean shouldValidate = Maps.getBoolean("validate", args, true)
        boolean failOnError = Maps.getBoolean("failOnError", args, true)
        if(shouldValidate){
            boolean valid = gormValidationApi().validate entity
            if(!valid && failOnError){
                throw ValidationProblem.of(entity).errors(((GormEntity)entity).errors).toException()
            }
            return valid
        }
        return true
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
     * @throws NotFoundProblem.Exception if mustExist is true, and entity not found
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
        //if data is empty then fire exception
        RepoUtil.checkData(data, entityClass)

        // throw error if id is passed in but bindId is false
        if(BindAction.Create == bindAction && data.id){
            RepoUtil.checkCreateData(data, args,  entityClass)
            if(args.bindId) entity['id'] = data['id']
        }

        args['bindAction'] = bindAction
        bind(entity, data, bindAction, args)

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
     * @throws NotFoundProblem.Exception if its not found or if a DataIntegrityViolationException is thrown
     */
    void removeById(Serializable id, Map args = [:]) {
        withTrx {
            D entity = get(id, null)
            doRemove(entity)
        }
    }

    /**
     * Transactional, Calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions.
     *
     * @param entity the domain entity
     * @throws ValidationProblem.Exception if a spring DataIntegrityViolationException is thrown
     */
    void remove(D entity, Map args = [:]) {
        withTrx {
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
     * @throws NotFoundProblem.Exception if its not found
     * @throws ValidationProblem.Exception if the versions mismatch
     */
    D get(Serializable id, Long version) {
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
    D get(Serializable id) {
        (D)gormStaticApi().get(id)
    }

    /**
     * wraps get in a trx. NOT a read only trx like read as that messes with dirty tracking
     * @param id required, the id to get
     * @return the retrieved entity
     */
    D getWithTrx(Serializable id) {
        entityTrx {
            return (D)gormStaticApi().get(id)
        }
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
     * Implement this property to list the association names that should be persisted first in this order
     * Used in persistToOneAssociations
     */
    List<String> getToOneAssociations(){
        return []
    }

    /**
     * For "ext" type associations that belong to the entity and have assignable id, we want to persist them first
     * so that hibernate does not query the database to check existance. Also, the hibernate settings for
     * order_inserts and order_updates, which are critical for decent perfromance when inserting large sets
     * does not get picked up unless we persist these first
     *
     * @param entity the main entity for this repo
     * @param associations the list of association names to do persist first if set
     */
    void persistToOneAssociations(D entity, List<String> assoctiations){
        for(String fld: assoctiations){
            def assoc = entity[fld]

            //use the getAssociationId as it deals with proxies and it doesn't hydrate the proxy
            if (assoc) {
                PersistableRepoEntity pentity = (PersistableRepoEntity)entity
                GormEntity gentity = (GormEntity)entity

                //if no id then its new so insert it
                if(!gentity.getAssociationId(fld)){
                    PersistableRepoEntity assocEntity = assoc as PersistableRepoEntity
                    assocEntity.id = pentity.id
                    assocEntity.persist(validate: false)
                }
                // TODO, after benchmark checks might need to also check if dirty and persist here.
            }
        }
    }

    /**
     * creates or updates associations for given entity, called during create or update methods
     *
     * @Param mainEntity The entity that has the associations that are being created/updated
     * @param assocRepo association entity repo
     * @param assocList the list of data maps to create/update
     * @param belongsToProp the name of parent property to set, if any
     * @return the list of created entities
     */
    List persistAssociationData(D entity, GormRepo assocRepo, List<Map> assocList, String belongsToProp = null){
        if(belongsToProp) assocList.each { it[belongsToProp] = entity}
        assocRepo.createOrUpdate(assocList)
    }

    /**
     * Creates or updates a list of items
     * Will rollback on any error
     *
     * @param dataList the list of data maps to create/update
     * @return the list of created entities
     */
    List<D> createOrUpdate(List<Map> dataList){
        List resultList = [] as List<D>

        dataList.each { Map item ->
            resultList << createOrUpdate(item)
        }

        return resultList
    }

    /** gets the datastore for this Gorm domain instance */
    Datastore getDatastore() {
        gormInstanceApi().datastore
    }

    /** flush on the datastore's currentSession.*/
    void flush(){
        TrxService.flush(getDatastore())
    }

    /** cache clear on the datastore's currentSession.*/
    void clear(){
        TrxService.clear(getDatastore())
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

    public <T> T withNewTrx(Closure<T> callable) {
        def trxAttr = new CustomizableRollbackTransactionAttribute()
        trxAttr.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
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

    void clear(TransactionStatus status) {
        TransactionObject txObject = (status as DefaultTransactionStatus).transaction as TransactionObject
        txObject.sessionHolder.getSession().clear()
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
