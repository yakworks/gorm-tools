package gorm.tools.dao

import gorm.tools.GormUtils
import gorm.tools.hibernate.criteria.CriteriaUtils
import gorm.tools.mango.MangoCriteria
import grails.converters.JSON
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import grails.plugin.dao.DaoMessage
import grails.plugin.dao.DaoUtil
import grails.plugin.dao.DomainException
import grails.validation.ValidationException
import groovy.transform.CompileDynamic
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

/**
 *
 * A trait that turns a class into a DAO
 *
 * @author Joshua Burnett
 */
trait GormDao<D> {

    Class<D> thisDomainClass // the domain class this is for

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
    @Transactional
    D save(D entity, Map args = [:]) {
        return doSave(entity, args)
    }

    D doSave(D entity, Map args) {
        //force fail on error
        args['failOnError'] = true
        try {
            beforeSave(entity)
            entity.save(args)
            afterSave(entity)
            return entity
        }
        catch (ValidationException ve) {
            fireSaveError(entity, ve)
        }
        catch (DataAccessException dae) {
            fireSaveError(entity, dae)
        }
    }

    /**
     * Calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions.
     *
     * @param entity the domain entity
     * @throws DomainException if a spring DataIntegrityViolationException is thrown
     */
    @Transactional
    void delete(D entity) {
        doDelete(entity)
    }

    void doDelete(D entity) {
        try {
            beforeDelete(entity)
            entity.delete(flush: true)
        }
        catch (DataIntegrityViolationException dae) {
            fireDeleteError(entity, dae)
        }
    }

    D createNew(Map data, Map args = [:]) {
        D entity = domainClass.newInstance()
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
        save(entity, args)
        return entity
    }

    void bindCreate(D entity, Map data, Map args = [:]){
        bind(entity, data, args)
    }

    /**
     * Updates a new domain entity with the data from params.
     *
     * @param params the parameter map
     * @throws DomainException if a validation error happens or its not found with the params.id
     *                         or the version is off and someone else edited it
     */
    @Transactional
    D update(Map params) {
        return doUpdate(params)
    }

    D doUpdate(Map params) {
        D entity = get(params.id as Serializable)

        DaoUtil.checkFound(entity, params, domainClass.name)
        DaoUtil.checkVersion(entity, params.version)

        entity.properties = params
        if (fireEvents) beforeUpdateSave(entity, params)
        save(entity)
        return [ok: true, entity: entity, message: null]
    }

    /**
     * Uses
     *
     * @param params
     * @param closure
     * @return
     */
    @Transactional(readOnly = true)
    List<D> query(Map params) {
        Map criteria = params['criteria']
        MangoCriteria mangoCriteria = new MangoCriteria(D)
        mangoCriteria.build(criteria)
        return mangoCriteria.list(params)
//        if (criteria instanceof String) { //TODO: keyWord `criteria` probably should be driven from config
//            JSON.use('deep')
//            criteria = JSON.parse(params['criteria']) as Map
//        } else {
//            criteria = params['criteria'] as Map ?: [:]
//        }
        //CriteriaUtils.list(criteria, this.thisDomainClass, params as Map, closure)
    }

    /**
     * Deletes a new domain entity base on the id in the params.
     *
     * @param params the parameter map that has the id for the domain entity to delete
     * @throws DomainException if its not found or if a DataIntegrityViolationException is thrown
     */
    @Transactional
    void remove(Serializable id) {
        doRemove(id)
    }

    void doRemove(Serializable id) {
        D entity = get(id)
        DaoUtil.checkFound(entity, params, domainClass.name)
        beforeRemoveSave(entity, params)
        Map msg =null // DaoMessage.deleted(entity, DaoMessage.badge(entity.ident(), entity))
        delete(entity)
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
        else if(dataBinder == 'fast'){
            GormUtils.bindFast(entity, row)
        }
        else {
            //fall back to just setting the props
            GormUtils.bindFast(entity, row)
        }
        return entity
    }

    D get(Serializable id) {
        return domainClass.get(id)
    }

    void fireEvent(String event, D entity) { }

}
