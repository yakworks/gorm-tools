package gorm.tools.dao

import gorm.tools.GormUtils
import gorm.tools.mango.MangoCriteria
import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.DaoUtil
import grails.plugin.dao.DomainException
import grails.validation.ValidationException
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.engine.event.EventType
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

/**
 *
 * A trait that turns a class into a DAO
 *
 * @author Joshua Burnett
 */
@GrailsCompileStatic
trait GormDao<D extends GormEntity> {

	String defaultDataBinder = 'grails'

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
        return doPersist(entity, args)
    }

    D doPersist(D entity, Map args = [:]) {
        try {
            fireEvent(DaoEventType.BeforeSave, entity)
            args['failOnError'] = args.containsKey('failOnError') ? args['failOnError'] : true
            entity.save(args)
            fireEvent(DaoEventType.AfterSave, entity)
            return entity
        }
        catch (ValidationException | DataAccessException ex) {
            handleException(entity, ex)
        }
    }

    /**
     * Creates a new instance and domain and binds the data using the bindCreate method.
     *
     * @param data
     * @param args
     * @return
     */
    D instance(Map data, Map args = [:]) {
        D entity = (D)domainClass.newInstance()
        String bindMethod = args?.containsKey("bindMethod") ? args.bindMethod : "bindCreate"
        if(bindMethod == "bindCreate"){
            bindCreate(entity, data, args)
        } else {
            callBinderMethod(bindMethod, entity, data, args)
        }
        return entity
    }

    D create(Map data, Map args = [:]) {
        D entity = createNew(data, args)
        persist(entity, args)
        return entity
    }

    void bindCreate(D entity, Map data, Map args = [:]){
        bind(entity, data, args)
    }


    D update(Map params) {
        return doUpdate(params)
    }

    D doUpdate(Map params) {
        D entity = get(params.id as Serializable)

        DaoUtil.checkFound(entity, params, domainClass.name)
        DaoUtil.checkVersion(entity, params.version)

		entity.properties = params

		fireEvent(DaoEventType.BeforeUpdate, entity, params)
        persist(entity)
		fireEvent(DaoEventType.AfterUpdate, entity, params)
        return entity
    }


    List<D> query(Map params) {
        Map criteria = params['criteria']
        MangoCriteria mangoCriteria = new MangoCriteria(D)
        mangoCriteria.build(criteria)
        return mangoCriteria.list(params)
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
    //@Transactional
    void remove(D entity) {
        doRemove(entity)
    }

    void doRemove(D entity) {
        try {
            fireEvent(DaoEventType.BeforeRemove, entity)
            entity.delete(flush:true)
            fireEvent(DaoEventType.AfterRemove, entity)
        }
        catch (DataIntegrityViolationException dae) {
            handleException(entity, dae)
        }
    }


    @CompileDynamic
    def callBinderMethod(String method, D entity, Map data, Map args = [:]){
        "${method}"(entity, data, args)
    }

    D bind(D entity, Map row, Map args = [:]){
        String dataBinder = args?.containsKey("dataBinder") ? args.dataBinder : defaultDataBinder
        if(dataBinder == 'grails'){
            entity.properties = row
        }
        else {
            GormUtils.bindFast(entity, row)
        }

        return entity
    }

    D get(Serializable id) {
        D entity = domainClass.get(id)
        DaoUtil.checkFound(entity, [id: id], domainClass.name)
        return entity
    }

    void fireEvent(DaoEventType eventType, Object... args) { }


    void handleException(D entity, RuntimeException e) throws DataAccessException {

    }

}
