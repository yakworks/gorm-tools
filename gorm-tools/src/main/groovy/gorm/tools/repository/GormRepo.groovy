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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider
import org.springframework.dao.DataAccessException
import org.springframework.transaction.TransactionDefinition

import gorm.tools.beans.EntityResult
import gorm.tools.databinding.BindAction
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.mango.api.QueryService
import gorm.tools.mango.api.QueryServiceLookup
import gorm.tools.mango.jpql.KeyExistsQuery
import gorm.tools.model.Lookupable
import gorm.tools.model.Persistable
import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.bulk.BulkableRepo
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.model.ApiCrudRepo
import gorm.tools.repository.model.PersistableRepoEntity
import gorm.tools.transaction.TrxUtils
import gorm.tools.utils.GormMetaUtils
import grails.core.support.proxy.ProxyHandler
import grails.validation.ValidationException
import yakworks.api.HttpStatus
import yakworks.api.problem.ThrowableProblem
import yakworks.api.problem.data.NotFoundProblem
import yakworks.commons.lang.ClassUtils

/**
 * A trait that turns a class into a Repository
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@SuppressWarnings(['EmptyMethod', 'MethodCount'])
@CompileStatic
trait GormRepo<D> implements ApiCrudRepo<D>, BulkableRepo<D>, ResolvableTypeProvider {

    @Autowired EntityMapBinder entityMapBinder

    @Autowired RepoEventPublisher repoEventPublisher

    @Autowired ProxyHandler proxyHandler

    //@Autowired(required=false)  //can't autowire, the DefaultGormRepo beans dont retain the D generic
    QueryService<D> queryService

    /** default to true. If false only method events are invoked on the implemented Repository. */
    Boolean enableEvents = true

    /**
     * The java class for the Gorm domain (persistence entity). will generally get set in constructor or using the generic as
     * done in {@link gorm.tools.repository.GormRepo#getEntityClass}
     * using the {@link org.springframework.core.GenericTypeResolver}
     * @see org.grails.datastore.mapping.model.PersistentEntity#getJavaClass().
     */
    Class<D> entityClass // the domain class this is for

    //cached instance of the query for id to keep it fast
    KeyExistsQuery idExistsQuery

    /**
     * The gorm domain class. uses the {@link org.springframework.core.GenericTypeResolver} is not set during contruction
     */
    @Override
    Class<D> getEntityClass() {
        if (!entityClass) this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), GormRepo)
        return entityClass
    }

    QueryService<D> getQueryService(){
        if (!this.queryService) {
            //creates a DefaultQueryService if one cant be found
            this.queryService = QueryServiceLookup.lookup(getEntityClass())
        }
        return this.queryService
    }

    @Override
    ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), getEntityClass())
    }

    /**
     * Transactional wrap for {@link #doPersist}
     * Saves a domain entity with the passed in args and rewraps ValidationException with EntityValidationException on error.
     *
     * @param entity the domain entity to call save on
     * @param args the arguments to pass to save.
     * @throws DataAccessException if a validation or DataAccessException error happens
     */
    D persist(D entity, PersistArgs args = PersistArgs.defaults()) {
        withTrx {
            return doPersist(entity, args)
        }
    }

    D persist(D entity, Map args) {
        return persist(entity, PersistArgs.of(args))
    }

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
    D doPersist(D entity, PersistArgs args) {
        try {
            generateId((Persistable)entity) //should only set id if its empty, does nothing here by default

            doBeforePersist(entity, args)
            getRepoEventPublisher().doBeforePersist(this, (GormEntity)entity, args)

            validateAndSave(entity, args)

            doAfterPersist(entity, args)
            getRepoEventPublisher().doAfterPersist(this, (GormEntity)entity, args )

            return entity
        }
        catch (ValidationException | DataAccessException ex) {
            throw RepoExceptionSupport.translateException(ex, entity)
        }
    }

    /**
     * called from doPersist. calls validate and save, no try catch so Exceptions bubble out.
     * Can be
     */
    void validateAndSave(D entity, PersistArgs args) {
        validate(entity, args)
        doAfterValidateBeforeSave(entity, args)
        args.validate = false //set it false so we dont do it again
        gormSave(entity, args)
    }

    /**
     * called from doPersist. calls validate and save, no try catch so Exceptions bubble out.
     * Can be
     */
    D gormSave(D entity, PersistArgs args) {
        gormInstanceApi().save(entity, args as Map)
    }

    /**
     * called right BEFORE validateAndSave to fire events but can be overridden too.
     * just be sure to fire event if overidden.
     * NOTE
     */
    void doBeforePersist(D entity, PersistArgs args){
        //empty, implement in concrete repo if needed
    }

    /**
     * called right AFTER validate and BEFORE save. calls associations persists but can be overridden too.
     * No events fired for this
     */
    void doAfterValidateBeforeSave(D entity, PersistArgs args){
        persistToOneAssociations(entity, getToOneAssociations())
    }

    /**
     * called right after save to fire events and call associations persists but can be overridden too.
     * just be sure to fire event if overidden.
     */
    void doAfterPersist(D entity, PersistArgs args){
        //empty, implement in concrete repo if needed
    }

    /**
     * validates the entity and throws a ValidationProblem if it fails
     */
    boolean validate(D entity, PersistArgs args) {
        if(args.validate != false){
            boolean valid = gormValidationApi().validate entity
            if(!valid && args.failOnError){
                def ex = ValidationProblem.ofEntity(entity).errors(((GormEntity)entity).errors).toException()
                // println ex
                throw ex
            }
            return valid
        }
        return true
    }

    /**
     * Transactional wrap for {@link #doCreate}
     */
    D create(Map data, PersistArgs args = PersistArgs.defaults()) {
        withTrx {
            return doCreate(data, args)
        }
    }

    D create(Map data, Map args) {
        create(data, PersistArgs.of(args))
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
    D doCreate(Map data, PersistArgs args) {
        D entity = (D) getEntityClass().newInstance()
        args.insert(true)
        bindAndCreate(entity, data, args)
        return entity
    }

    /**
     * just calls bindAndSave with the right BindAction
     * This is the best method to overrride if needed
     */
    void bindAndCreate(D entity, Map data, PersistArgs args) {
        bindAndSave(entity, data, BindAction.Create, args)
    }

    /**
     * Transactional wrap for {@link #doUpdate}
     */
    D update(Map data, PersistArgs args = PersistArgs.defaults()) {
        withTrx {
            return doUpdate(data, args)
        }
    }

    D update(Map data, Map args) {
        update(data, PersistArgs.of(args))
    }

    /**
     * Updates entity using the data from params. calls the {@link #bind} with bindMethod='Update'
     *
     * @param data the data to bind onto the entity
     * @return the updated domain entity
     * @see #doPersist
     */
    D doUpdate(Map data, PersistArgs args) {
        D entity = findWithData(data)
        bindAndUpdate(entity, data, args)
        return entity
    }

    /**
     * Update if it can find it with findWithData, otherwise it inserts it.
     * Uses findWithData and if instance is found then considers it an update.
     * NOT transactional so should be wrapped in a transaction.
     *
     * @param data what to update or insert
     * @param pargs the PersistArgs
     * @return The result with the entity
     */
    @Override
    EntityResult<D> upsert(Map data, PersistArgs pargs = PersistArgs.of()) {
        if (!data) return
        D instance
        try {
            instance = findWithData(data, false)
        } catch(NotFoundProblem.Exception nfe){
            //we pass mustExist=false to findWithData but if data has an 'id' prop
            // and it fails on the get then will throws a NotFoundProblem Ex.
            //if we want them inserted then args.bindId must be true as well, just like on an insert.
            // when bindId is not set then we throw the exception.
            if(!pargs.bindId) throw nfe
        }
        HttpStatus status
        if (instance) {
            //dont call update or doUpdate as it will do findWithData again.
            bindAndUpdate(instance, data, pargs)
            status = HttpStatus.OK
        } else {
            instance = doCreate(data, pargs)
            status = HttpStatus.CREATED
        }
        // Result.OK().status(status).payload(instance)
        return EntityResult.of(instance).status(status)
    }

    /**
     * Uses the items in the data to find the entity.
     * If data has an id key the use that.
     * otherwise it will see if entity has lookupable or if the concrete repo has a lookup method
     *
     * @param data: the map with the keys for lookup
     * @param mustExist: if the record must exist. If true then throws error if can't be found.
     * @return the found entity. If mustExist=false then can return null
     * @throws NotFoundProblem.Exception if mustExist is true, and entity not found
     */
    D findWithData(Map data, boolean mustExist = true) {
        D foundEntity
        def ident = data['id'] as Serializable
        //check by id first
        if(ident){
            //return it fast if its good to go, will blown an error if not found
            //if this is an upsert though we want to create it with the id if bindId=true
            return get(ident, data['version'] as Long)
        }
        if(Lookupable.isAssignableFrom(getEntityClass())){
            // call the lookup if domain implements the Lookupable
            //FIXME is there a cleaner way to do this?
            foundEntity = (D) ClassUtils.callStaticMethod(getEntityClass(), 'lookup', data)
        }
        if(!foundEntity) {
            foundEntity = lookup(data)
        }
        if(mustExist) RepoUtil.checkFound(foundEntity, data as Serializable, getEntityClass().name)
        return foundEntity
    }

    /**
     * does nothing, can be implemented by the conrete repo for special lookup logic such as for souceId
     * if nothing is found it should return null or throw a DataRetrievalFailureException if message needed
     */
    D lookup(Map data) {
        return null
    }

    /**
     * if entity.id is null then generates and assigns new id to id property on entity, if entity.id is already set then it just returns it
     * does nothing by default, entities that have to generate an Id can override
     */
    Serializable generateId(Persistable entity){
        //override in impl that needs this.
        return null as Serializable
    }

    void bindAndUpdate(D entity, Map data, PersistArgs args) {
        bindAndSave(entity, data, BindAction.Update, args)
    }

    /** short cut to call {@link #bind}, setup args for events then calls {@link #doPersist} */
    void bindAndSave(D entity, Map data, BindAction bindAction, PersistArgs args){
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
        if(args.persistAfterAction != false) doPersist(entity, args)

    }

    /**
     * binds by calling {@link #doBind} and fires before and after events
     * better to override doBind in implementing classes for custom binding logic.
     * Or even better implement the beforeBind|afterBind event methods
     */
    void bind(D entity, Map data, BindAction bindAction, PersistArgs args = new PersistArgs()) {
        //if its a PathKeyMap then init it
        // if(data instanceof PathKeyMap) data.init()

        getRepoEventPublisher().doBeforeBind(this, (GormEntity)entity, data, bindAction, args)
        doBind(entity, data, bindAction, args)
        getRepoEventPublisher().doAfterBind(this, (GormEntity)entity, data, bindAction, args)
    }

    /**
     * Main bind method that redirects call to the injected mapBinder.
     * override this one in implementing classes.
     * can also call this if you do NOT want the before/after Bind events to fire
     */
    void doBind(D entity, Map data, BindAction bindAction, PersistArgs args) {
        getEntityMapBinder().bind(args as Map, entity, data)
    }

    /**
     * Remove by ID, this does not wrap a trx
     *
     * @param id - the id to delete
     * @param args - the args to pass to delete. flush being the most common
     *
     * @throws NotFoundProblem.Exception if its not found or DataProblemException if a DataIntegrityViolationException is thrown
     */
    void removeById(Serializable id, PersistArgs args) {
        try {
            D entity = get(id)
            RepoUtil.checkFound(entity, id, getEntityClass().name)
            doRemove(entity, args)
        }
        catch (DataAccessException ex) {
            // attempt to catch exception (eg fk violation), if occurs, but most of the time this wont if its wrapped in a Trx
            // would occur during flush (either if passed in or at commit in end of transaction).
            // So this removeById either needs to be wrapped in try/catch with problemHandler or flush should be passed in here
            throw RepoExceptionSupport.translateException(ex, id)
        }
    }

    /**
     * Calls delete, if you want the error handling to pic up and translate fk error then pass in flush()
     *
     * @param entity the domain entity
     * @throws ThrowableProblem if a DataIntegrityViolationException is thrown
     */
    void remove(D entity, PersistArgs args = PersistArgs.defaults()) {
        try {
            doRemove(entity, args)
        }
        catch (DataAccessException ex) {
            //see comments in removeById about when or if this ever gets picked up
            throw RepoExceptionSupport.translateException(ex, entity)
        }
    }

    /**
     * no trx wrapper. delete entity.
     *
     * @param entity - the domain instance to delete
     * @param args - args passed to delete
     */
    void doRemove(D entity, PersistArgs args) {
        getRepoEventPublisher().doBeforeRemove(this, (GormEntity)entity, args)
        gormInstanceApi().delete(entity, args as Map)
        getRepoEventPublisher().doAfterRemove(this, (GormEntity)entity, args)
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
     * wraps get in a trx, usefull for testing. NOT a read only trx like read as that messes with dirty tracking
     * @param id the id to get
     * @return the retrieved entity
     */
    D getWithTrx(Serializable id) {
        withTrx {
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
        (D) gormStaticApi().read(id)
    }

    /**
     * load without hydrating
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
        return ( ClassUtils.getStaticPropertyValue(entityClass, "toOneAssociations") ?: [] ) as List<String>
    }

    /**
     * Called beforeSave
     * Specific design pattern for One-to-One associations that belong to this entity
     * and have an assignable id that is the same as this
     * We want to persist these before the save because
     *  1. this entity stores a ref to their key (which is the same as this one)
     *  2. so that hibernate does not query the database to check if it exists.
     * Also, the hibernate settings for order_inserts and order_updates, which are critical for decent perfromance when
     * inserting large sets does not get picked up unless we persist these first
     *
     * @param entity the main entity for this repo
     * @param associations the list of association names to do persist first if set
     */
    void persistToOneAssociations(D entity, List<String> assoctiations){
        for(String assocName: assoctiations){
            def assoc = entity[assocName]
            if (assoc) {
                //if its a proxy then its already setup and not new
                //so check that its not a proxy and that it doesnt have an id for it already
                //use the getAssociationId as it deals with proxies and it doesn't hydrate the proxy
                if(!proxyHandler.isProxy(assoc) && !((GormEntity)entity).getAssociationId(assocName)){
                    PersistableRepoEntity assocEntity = assoc as PersistableRepoEntity
                    assocEntity.id = ((PersistableRepoEntity)entity).getId()
                    assocEntity.persist(new PersistArgs(validate:false, insert: true))
                }
                // TODO, after benchmark checks might need to also check if dirty and persist here.
            }
        }
    }

    /**
     * helper for createOrUpdate with One-to-Many association on the entity.
     * Will most often be called from an overriden doAfterPersist with data.
     * 1. iterates over the List of Maps and assigns the childrens belongs to key to the entity intance
     * 2. uses the passed in repo to call the createOrUpdate or the List
     *
     * @param mainEntity The entity that has the associations that are being created/updated
     * @param assocRepo association entity repo
     * @param assocList the list of data maps to create/update
     * @param belongsToProp the name of parent property to set, if any
     * @return the list of created entities
     */
    @SuppressWarnings('ReturnsNullInsteadOfEmptyCollection')
    List persistToManyData(D entity, GormRepo assocRepo, List<Map> assocList, String belongsToProp = null){
        if(!assocList) return
        if(belongsToProp) assocList.each { it[belongsToProp] = entity}
        assocRepo.createOrUpdate(assocList)
    }

    /**
     * Calls upsert on a list of items.
     * Use for small datasets, use bulk operations for larger datasets.
     * iterates and just calls createOrUpdateItem, no special handling for data.op
     * NOT Transactional, so should be wrapped in a trx to function properly.
     * Will throw exception on any error.
     *
     * @param dataList the list of data maps to create/update
     * @return the list of created or updated entities
     */
    List<D> createOrUpdate(List<Map> dataList, PersistArgs pargs = PersistArgs.of()){
        List resultList = [] as List<D>

        dataList.each { Map item ->
            resultList << upsert(item, pargs.clone()).entity
        }

        return resultList
    }

    /**
     * Performant way to check if id exists in database.
     * Wrapped in read-only trx so dont need to setup a @Transactional method to do a quick check in code
     */
    @Override
    boolean exists(Serializable id) {
        withReadOnlyTrx {
            if (!idExistsQuery) idExistsQuery = KeyExistsQuery.of(getEntityClass())
            return idExistsQuery.exists(id)
        }
    }

    /** gets the datastore for this Gorm domain instance */
    Datastore getDatastore() {
        gormInstanceApi().datastore
    }

    /** flush on the datastore's currentSession.*/
    void flush(){
        TrxUtils.flush(getDatastore())
    }

    /** cache clear on the datastore's currentSession.*/
    void clear(){
        TrxUtils.clear(getDatastore())
    }

    void flushAndClear() {
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
    boolean isNew(Persistable entity, PersistArgs args) {
        args.insert || entity.isNew()
    }

    /**
     * checks if its new or if its dirty
     */
    boolean isNewOrDirty(GormEntity entity) {
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
    public <T> T withTrx(Closure<T> callable) {
        def trxAttr = new CustomizableRollbackTransactionAttribute()
        withTrx(trxAttr, callable)
    }

    public <T> T withTrx(TransactionDefinition trxAttr, Closure<T> callable) {
        try {
            gormStaticApi().withTransaction(trxAttr, callable)
        } catch(ValidationException | DataAccessException ex) {
            //Many of the exceptions such as PK/FK constraint failures, Optimistic locking failures will happen when transaction commits
            throw RepoExceptionSupport.translateException(ex, null)
        }
    }

    public <T> T withNewTrx(Closure<T> callable) {
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
    public <T> T withReadOnlyTrx(Closure<T> callable) {
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

    GormValidationApi gormValidationApi() {
        GormEnhancer.findValidationApi(getEntityClass())
    }


}
