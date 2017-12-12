package gorm.tools.dao

import gorm.tools.TrxService
import gorm.tools.dao.errors.DomainException
import gorm.tools.dao.errors.DomainNotFoundException
import gorm.tools.dao.events.DaoEventPublisher
import gorm.tools.databinding.FastBinder
import gorm.tools.mango.DaoQuery
import grails.gorm.transactions.TransactionService
import grails.validation.ValidationException
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.core.Datastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

/**
 *
 * A trait that turns a class into a DAO
 *
 * @author Joshua Burnett
 */
@CompileStatic
trait GormDao<D extends GormEntity> implements DaoQuery, DaoApi<D>{

    @Autowired FastBinder dataBinder
    @Autowired DaoEventPublisher daoEventPublisher
    @Autowired TrxService trxService

    //use getters when accessing domainClass so implementing class can override the property if desired
    Class<D> domainClass // the domain class this is for

    @Override
    Class<D> getDomainClass() {
        if(!domainClass) this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), GormDao.class)
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
        trxService.withTrx {
            return doPersist(entity, args)
        }
    }

    D doPersist(D entity, Map args = [:]) {
        try {
            //DaoUtil.fireEvent(this, DaoEventType.BeforePersist, entity)
            daoEventPublisher.doBeforePersist(this, entity)
            args['failOnError'] = args.containsKey('failOnError') ? args['failOnError'] : true
            entity.save(args)
            daoEventPublisher.doAfterPersist(this, entity)
            //DaoUtil.fireEvent(this, DaoEventType.AfterPersist, entity)
            return entity
        }
        catch (ValidationException | DataAccessException ex) {
            throw handleException(entity, ex)
        }
    }

    @Override
    D create(Map params) {
        trxService.withTrx {
            return doCreate(params)
        }
    }

    D doCreate(Map params) {
        D entity = (D) getDomainClass().newInstance()
        //watch for the http://docs.groovy-lang.org/next/html/documentation/core-traits.html#_inheritance_of_state_gotchas, use getters
        daoEventPublisher.doBeforeCreate(this, entity, params)
        bindAndSave(entity, params, "Create")
        daoEventPublisher.doAfterCreate(this, entity, params)
        return entity
    }

    @Override
    D update(Map params) {
        trxService.withTrx {
            return doUpdate(params)
        }
    }

    D doUpdate(Map params) {
        D entity = get(params)
        daoEventPublisher.doBeforeUpdate(this, entity, params)
        bindAndSave(entity, params, "Update")
        daoEventPublisher.doAfterUpdate(this, entity, params)
        return entity
    }

    @Override
    //@CompileDynamic
    D bindAndSave(D entity, Map params, String bindMethod) {
        bind(entity, params, bindMethod)
        doPersist(entity)
        return entity
    }

    @Override
    void bind(D entity, Map row, String bindMethod = null){
        getDataBinder().bind(entity, row, bindMethod)
    }

    /**
     * Deletes a new domain entity base on the id in the params.
     *
     * @param params the parameter map that has the id for the domain entity to delete
     * @throws DomainException if its not found or if a DataIntegrityViolationException is thrown
     */
    @Override
    void removeById(Serializable id) {
        D entity = get(id, null)
        remove(entity)
    }

    /**
     * Calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions.
     *
     * @param entity the domain entity
     * @throws DomainException if a spring DataIntegrityViolationException is thrown
     */
    @Override
    void remove(D entity) {
        trxService.withTrx {
            doRemove(entity)
        }
    }

    void doRemove(D entity) {
        try {
            daoEventPublisher.doBeforeRemove(this, entity)
            entity.delete(flush:true)
            daoEventPublisher.doAfterRemove(this, entity)
        }
        catch (DataIntegrityViolationException dae) {
            throw handleException(entity, dae)
        }
    }

    /**
     * gets and verfiies that the enity can eb retireved and version matches.
     *
     * @param id required, the id to get
     * @param version - can be null. if its passed in then it validates its that same as the version in the retrieved entity.
     * @return the retrieved entity. Will always be an entity as this throws an error if not
     *
     * @throws DomainNotFoundException if its not found
     * @throws DomainException if the versions mismatch
     */
    @Override
    D get(Serializable id, Long version) throws DomainNotFoundException, DomainException {
        D entity = GormEnhancer.findStaticApi(getDomainClass()).get(id)
        DaoUtil.checkFound(entity, [id: id], getDomainClass().name)
        if(version != null) DaoUtil.checkVersion(entity, version)
        return entity
    }

    /**
     * calls {@link #get(Serializable id, Long version)}
     *
     * @param params expects a Map with an id key and optionally a version
     * @return
     */
    @Override
    D get(Map params) {
        return get(params.id as Serializable, params.version as Long)
    }

    @Override
    DomainException handleException(D entity, RuntimeException e) {
        return DaoUtil.handleException(entity, e)
    }

    public <T> T withTrx(Map transProps = [:], Closure<T> callable) {
        //this seems to be faster than the withTransaction on the static gorm api. the TrxService seems to be as fast
        TransactionService txService = getDatastore().getService(TransactionService)
        txService.withTransaction(transProps, callable)
        //GormEnhancer.findStaticApi(getDomainClass()).withTransaction(transProps, callable)
        //callable()
    }

    Datastore getDatastore(){
        GormEnhancer.findInstanceApi(domainClass).datastore
    }

//    public <T> T withTransaction(Closure<T> callable) {
//        GormEnhancer.findStaticApi(getDomainClass()).withTransaction(callable)
//    }

}
