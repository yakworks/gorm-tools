package gorm.tools.repository

import gorm.tools.WithTrx
import gorm.tools.repository.errors.DomainException
import gorm.tools.repository.errors.DomainNotFoundException
import gorm.tools.databinding.MapBinder
import gorm.tools.mango.DaoQuery
import gorm.tools.repository.events.RepoEventPublisher
import grails.validation.ValidationException
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.gorm.GormInstanceApi
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.core.Datastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

/**
 * A trait that turns a class into a DAO
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@CompileStatic
trait GormRepo<D extends GormEntity> implements GormBatchRepo<D>, DaoQuery, WithTrx, RepositoryApi<D> {

    @Autowired
    MapBinder mapBinder
    @Autowired
    RepoEventPublisher repoEventPublisher

    boolean enableEvents = true

    //use getters when accessing domainClass so implementing class can override the property if desired
    Class<D> domainClass // the domain class this is for

    //the cached datastore
    Datastore datastore

    @Override
    Class<D> getDomainClass() {
        if (!domainClass) this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), GormRepo.class)
        return domainClass
    }

    /**
     * Saves a domain entity with the passed in args and rewraps ValidationException with DomainException on error.
     *
     * @param entity the domain entity to call save on
     * @param args the arguments to pass to save
     * @throws DataAccessException if a validation or DataAccessException error happens
     */
    @Override
    D persist(D entity, Map args = [:]) {
        withTrx {
            return doPersist(entity, args)
        }
    }

    D doPersist(D entity, Map args = [:]) {
        try {
            args['failOnError'] = args.containsKey('failOnError') ? args['failOnError'] : true
            getRepoEventPublisher().doBeforePersist(this, entity, args)
            entity.save(args)
            getRepoEventPublisher().doAfterPersist(this, entity, args)
            return entity
        }
        catch (ValidationException | DataAccessException ex) {
            throw handleException(entity, ex)
        }
    }

    @Override
    D create(Map params) {
        withTrx {
            return doCreate(params)
        }
    }

    D doCreate(Map params, Map args = [:]) {
        D entity = (D) getDomainClass().newInstance()
        getRepoEventPublisher().doBeforeCreate(this, entity, params)
        bind(entity, params, "Create")
        doPersist(entity, args)
        getRepoEventPublisher().doAfterCreate(this, entity, params)
        return entity
    }

    @Override
    D update(Map params) {
        withTrx {
            return doUpdate(params)
        }
    }

    @Override
    D doUpdate(Map params, Map args = [:]) {
        D entity = get(params)
        getRepoEventPublisher().doBeforeUpdate(this, entity, params)
        bind(entity, params, "Update")
        doPersist(entity, args)
        getRepoEventPublisher().doAfterUpdate(this, entity, params)
        return entity
    }

    /**
     * Convenience method to call {@link #bind} and then {@link #doPersist}
     */
//    D bindAndSave(D entity, Map params, String bindMethod) {
//        bind(entity, params, bindMethod)
//        doPersist(entity)
//        return entity
//    }

    @Override
    void bind(D entity, Map data, String bindMethod = null) {
        getMapBinder().bind(entity, data, bindMethod)
    }

    /**
     * Deletes a new domain entity base on the id in the params. Non Trx
     *
     * @param params the parameter map that has the id for the domain entity to delete
     * @throws DomainException if its not found or if a DataIntegrityViolationException is thrown
     */
    @Override
    void removeById(Serializable id, Map args = [:]) {
        D entity = getStaticApi().load(id)
        doRemove(entity)
    }

    /**
     * Transactional, Calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions.
     *
     * @param entity the domain entity
     * @throws DomainException if a spring DataIntegrityViolationException is thrown
     */
    @Override
    void remove(D entity) {
        withTrx {
            doRemove(entity)
        }
    }

    void doRemove(D entity, Map args = [:]) {
        try {
            getRepoEventPublisher().doBeforeRemove(this, entity)
            entity.delete(flush: true)
            getRepoEventPublisher().doAfterRemove(this, entity)
        }
        catch (DataIntegrityViolationException dae) {
            throw handleException(entity, dae)
        }
    }

    /**
     * gets and verfiies that the entity can eb retireved and version matches.
     *
     * @param id required, the id to get
     * @param version - can be null. if its passed in then it validates its that same as the version in the retrieved entity.
     * @return the retrieved entity. Will always be an entity as this throws an error if not
     *
     * @throws DomainNotFoundException if its not found
     * @throws DomainException if the versions mismatch
     */
    @Override
    D get(Serializable id, Long version) {
        D entity = getStaticApi().get(id)
        RepoUtil.checkFound(entity, [id: id], getDomainClass().name)
        if (version != null) RepoUtil.checkVersion(entity, version)
        return entity
    }

    /**
     * calls {@link #get(Serializable id, Long version)}
     *
     * @param params expects a Map with an id key and optionally a version
     * @return
     */
    @Override
    D get(Map<String, Object> params) {
        return get(params.id as Serializable, params.version as Long)
    }

    @Override
    DomainException handleException(D entity, RuntimeException e) {
        return RepoUtil.handleException(entity, e)
    }

    Datastore getDatastore() {
        getInstanceApi().datastore
    }

    GormInstanceApi<D> getInstanceApi() {
        (GormInstanceApi<D>) GormEnhancer.findInstanceApi(getDomainClass())
    }

    GormStaticApi<D> getStaticApi() {
        (GormStaticApi<D>) GormEnhancer.findStaticApi(getDomainClass())
    }
}
