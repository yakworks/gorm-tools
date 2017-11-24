package gorm.tools.dao

import gorm.tools.databinding.FastBinder
import gorm.tools.mango.MangoCriteria
import grails.plugin.dao.DaoUtil
import grails.plugin.dao.DomainException
import grails.validation.ValidationException
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

/**
 *
 * A trait that turns a class into a DAO
 *
 * @author Joshua Burnett
 */
@CompileStatic
trait GormDao<D extends GormEntity> {

	FastBinder fastBinder

    private Class<D> thisDomainClass // the domain class this is for

    Class<D> getDomainClass() { return thisDomainClass }
    //set this is constructing a base dao by hand
    void setDomainClass(Class<D> clazz) { thisDomainClass = clazz }

    /**
     * Saves a domain entity with the passed in args and rewraps ValidationException with DomainException on error.
     *
     * @param entity the domain entity to call save on
     * @param args the arguments to pass to save
     * @throws grails.plugin.dao.DomainException if a validation or DataAccessException error happens
     */
    D persist(D entity, Map args = [:]) {
        withTransaction {
            return doPersist(entity, args)
        }
    }

    D doPersist(D entity, Map args = [:]) {
        try {
            DaoUtil.fireEvent(DaoEventType.BeforePersist, entity)

            args['failOnError'] = args.containsKey('failOnError') ? args['failOnError'] : true
            entity.save(args)

            DaoUtil.fireEvent(DaoEventType.AfterPersist, entity)
            return entity
        }
        catch (ValidationException | DataAccessException ex) {
            throw handleException(entity, ex)
        }
    }

    D create(Map params, Map saveArgs = [:]) {
        D entity = (D)domainClass.newInstance()
        withTransaction {
            return bindAndSave("Create", entity, params, saveArgs)
        }

    }

    D update(Map params, Map saveArgs = [:]) {
        D entity = get(params)
        withTransaction {
            return bindAndSave("Update", entity, params, saveArgs)
        }
    }

    D bindAndSave(String bindMethod, D entity, Map params, Map saveArgs) {
        DaoUtil.fireEvent(DaoEventType.valueOf("Before$bindMethod"), entity, params)
        bind(bindMethod, entity, params)
        persist(entity, saveArgs)
        DaoUtil.fireEvent(DaoEventType.valueOf("After$bindMethod"), entity, params)
        return entity
    }

    /**
     * Deletes a new domain entity base on the id in the params.
     *
     * @param params the parameter map that has the id for the domain entity to delete
     * @throws DomainException if its not found or if a DataIntegrityViolationException is thrown
     */
    void removeById(Serializable id) {
        D entity = get(id)
        remove(entity)
    }

    /**
     * Calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions.
     *
     * @param entity the domain entity
     * @throws DomainException if a spring DataIntegrityViolationException is thrown
     */
    void remove(D entity) {
        withTransaction {
            doRemove(entity)
        }
    }

    void doRemove(D entity) {
        try {
            DaoUtil.fireEvent(DaoEventType.BeforeRemove, entity)
            entity.delete(flush:true)
            DaoUtil.fireEvent(DaoEventType.AfterRemove, entity)
        }
        catch (DataIntegrityViolationException dae) {
            handleException(entity, dae)
        }
    }

    D bind(String method, D entity, Map row){
        //TODO pass the bind type into fast binder
        (D) fastBinder.bind(method, entity, row)
    }

    D get(Serializable id, Long version = null) {
        D entity = GormEnhancer.findStaticApi(domainClass).get(id)
        DaoUtil.checkFound(entity, [id: id], domainClass.name)
        if(version != null) DaoUtil.checkVersion(entity, version)
        return entity
    }

    D get(Map params) {
        return get(params.id as Serializable, params.version as Long)
    }

    List<D> query(Map params) {
        Map criteria = params['criteria']
        MangoCriteria mangoCriteria = new MangoCriteria(D)
        mangoCriteria.build(criteria)
        return mangoCriteria.list(params)
    }

    DataAccessException handleException(D entity, RuntimeException e) throws DataAccessException {
        return DaoUtil.handleException(entity, e)
    }

    public <T> T withTransaction(Closure<T> callable) {
        GormEnhancer.findStaticApi(domainClass).withTransaction callable
    }

}
