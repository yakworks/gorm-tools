package gorm.tools.repository

import gorm.tools.WithTrx
import gorm.tools.databinding.BindAction
import gorm.tools.databinding.MapBinder
import gorm.tools.mango.api.MangoQueryTrait
import gorm.tools.repository.api.GormBatchRepo
import gorm.tools.repository.api.RepositoryApi
import gorm.tools.repository.errors.EntityNotFoundException
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.repository.errors.RepoExceptionSupport
import gorm.tools.repository.events.RepoEventPublisher
import grails.validation.ValidationException
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.gorm.GormInstanceApi
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.core.Datastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.GenericTypeResolver
import org.springframework.dao.DataAccessException

/**
 * A trait that turns a class into a Repository
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@CompileStatic
trait GormRepo<D extends GormEntity> implements GormBatchRepo<D>, MangoQueryTrait, WithTrx, RepositoryApi<D> {

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
        if (!entityClass) this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), GormRepo.class)
        return entityClass
    }

    /**
     * Transactional wrap for {@link #doPersist}
     * Saves a domain entity with the passed in args and rewraps ValidationException with EntityValidationException on error.
     *
     * @param entity the domain entity to call save on
     * @param args the arguments to pass to save
     * @throws DataAccessException if a validation or DataAccessException error happens
     */
    @Override
    D persist(Map args = [:], D entity) {
        withTrx {
            return doPersist(args, entity)
        }
    }

    /**
     * saves a domain entity with the passed in args.
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
            getRepoEventPublisher().doBeforePersist(this, entity, args)
            entity.save(args)
            getRepoEventPublisher().doAfterPersist(this, entity, args)
            return entity
        }
        catch (ValidationException | DataAccessException ex) {
            throw handleException(ex, entity)
        }
    }

    /**
     * Transactional wrap for {@link #doCreate}
     * createTrx(data)
     */
    @Override
    D create(Map args = [:], Map data) {
        withTrx {
            return doCreate(args, data)
        }
    }

    /**
     * Creates entity using the data from params. calls the {@link #bind} with bindMethod='Create'
     *
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
            return doUpdate(args, data)
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
        D entity = get(data)
        bindAndSave(args, entity, data, BindAction.Update)
        return entity
    }

    /** short cut to call {@link #bind}, setup args for events then calls {@link #doPersist} */
    void bindAndSave(Map args, D entity, Map data, BindAction bindAction){
        args['bindAction'] = bindAction
        bind(args, entity, data, bindAction)
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
        getRepoEventPublisher().doBeforeBind(this, entity, data, bindAction)
        doBind(args, entity, data, bindAction)
        getRepoEventPublisher().doAfterBind(this, entity, data, bindAction)
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
        D entity = get([id: id])
        doRemove(entity)
    }

    /**
     * Transactional, Calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions.
     *
     * @param entity the domain entity
     * @throws EntityValidationException if a spring DataIntegrityViolationException is thrown
     */
    @Override
    void remove(Map args = [:], D entity) {
        withTrx {
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
        RepoUtil.checkFound(entity, entity?.ident(), getEntityClass().name)
        try {
            getRepoEventPublisher().doBeforeRemove(this, entity, args)
            entity.delete(args)
            getRepoEventPublisher().doAfterRemove(this, entity, args)
        }
        catch (DataAccessException ex) {
            throw handleException(ex, entity)
        }
    }

    /**
     * gets and verifies that the entity can be retrieved and version matches.
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
        D entity = getStaticApi().get(id)
        RepoUtil.checkFound(entity, [id: id], getEntityClass().name)
        if (version != null) RepoUtil.checkVersion(entity, version)
        return entity
    }

    /**
     * This default will redirect the call to {@link #get(Serializable id, Long version)}.
     * Implementing classes can override this and add custom finders
     * using another unique lookup key other than id, such as customer number or invoice number. Unlike the normal get(id)
     * This throws a EntityNotFoundException if nothing is found instead of returning a null.
     *
     * @param params - expects a Map with an id key and optionally a version, implementation classes can customize to work with more.
     * @return the entity. Won't return null, instead it throws an exception
     */
    @Override
    D get(Map params) {
        return get(params.id as Serializable, params.version as Long)
    }

    @Override
    RuntimeException handleException(RuntimeException ex, D entity) {
        return getRepoExceptionSupport().translateException(ex, entity)
    }

    /** gets the dataStore for this Gorm domain instance */
    Datastore getDatastore() {
        getInstanceApi().datastore
    }

    /** flush on the datastore's currentSession. When possible use the transactionStatus.flush(). see WithTrx trait */
    void flush(){
        getDatastore().currentSession.flush()
    }

    /** cache clear on the datastore's currentSession. When possible use the transactionStatus. see WithTrx trait  */
    void clear(){
        getDatastore().currentSession.clear()
    }

    GormInstanceApi<D> getInstanceApi() {
        (GormInstanceApi<D>) GormEnhancer.findInstanceApi(getEntityClass())
    }

    GormStaticApi<D> getStaticApi() {
        (GormStaticApi<D>) GormEnhancer.findStaticApi(getEntityClass())
    }
}
