package gorm.tools.dao

import gorm.tools.Pager
import gorm.tools.dao.errors.DomainNotFoundException
import gorm.tools.databinding.FastBinder
import gorm.tools.mango.MangoBuilder
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import gorm.tools.dao.errors.DomainException
import grails.validation.ValidationException
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
trait GormDao<D extends GormEntity> {

    @Autowired
    FastBinder dataBinder

    @Value('${gorm.tools.mango.criteriaKeyName:criteria}') //gets criteria keyword from config, if there is no, then uses 'criteria'
    String criteriaKeyName

    //use getters when accessing domainClass so implementing class can override the property if desired
    Class<D> domainClass // the domain class this is for

    Class<D> getDomainClass() {
        if(!domainClass) this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), GormDao.class)
        return domainClass
    }

    /**
     * Saves a domain entity with the passed in args and rewraps ValidationException with DomainException on error.
     *
     * @param entity the domain entity to call save on
     * @param args the arguments to pass to save
     * @throws DomainException if a validation or DataAccessException error happens
     */
    D persist(D entity, Map args = [:]) {
        withTransaction {
            return doPersist(entity, args)
        }
    }

    D doPersist(D entity, Map args = [:]) {
        try {
            DaoUtil.fireEvent(this, DaoEventType.BeforePersist, entity)

            args['failOnError'] = args.containsKey('failOnError') ? args['failOnError'] : true
            entity.save(args)

            DaoUtil.fireEvent(this, DaoEventType.AfterPersist, entity)
            return entity
        }
        catch (ValidationException | DataAccessException ex) {
            throw handleException(entity, ex)
        }
    }

    D create(Map params) {
        //watch for the http://docs.groovy-lang.org/next/html/documentation/core-traits.html#_inheritance_of_state_gotchas, use getters
        D entity = (D)getDomainClass().newInstance()
        withTransaction {
            return bindAndSave(entity, params, "Create")
        }

    }

    D update(Map params) {
        D entity = get(params)
        withTransaction {
            return bindAndSave(entity, params, "Update")
        }
    }

    D bindAndSave(D entity, Map params, String bindMethod) {
        DaoUtil.fireEvent(this, DaoEventType.valueOf("Before$bindMethod"), entity, params)
        bind(entity, params, bindMethod)
        persist(entity)
        DaoUtil.fireEvent(this, DaoEventType.valueOf("After$bindMethod"), entity, params)
        return entity
    }

    D bind(D entity, Map row, String bindMethod){
        (D) getDataBinder().bind(entity, row, bindMethod)
    }

    /**
     * Deletes a new domain entity base on the id in the params.
     *
     * @param params the parameter map that has the id for the domain entity to delete
     * @throws DomainException if its not found or if a DataIntegrityViolationException is thrown
     */
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
    void remove(D entity) {
        withTransaction {
            doRemove(entity)
        }
    }

    void doRemove(D entity) {
        try {
            DaoUtil.fireEvent(this, DaoEventType.BeforeRemove, entity)
            entity.delete(flush:true)
            DaoUtil.fireEvent(this, DaoEventType.AfterRemove, entity)
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
    D get(Map params) {
        return get(params.id as Serializable, params.version as Long)
    }

    /**
     * Builds detached criteria for dao's domain based on mango criteria language and additional criteria
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    @CompileDynamic
    DetachedCriteria buildCriteria(Map params = [:], Closure closure = null){
        Map criteria
        if (params[criteriaKeyName] instanceof String) {
            JSON.use('deep')
            criteria = JSON.parse(params[criteriaKeyName]) as Map
        } else {
            criteria = params[criteriaKeyName] as Map ?: [:]
        }
        if (params.containsKey('sort')){
            criteria['$sort'] = params['sort']
        }
        MangoBuilder.build(getDomainClass(), criteria, closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    @CompileDynamic
    List<D> query(Map params = [:], Closure closure = null) {
        withTransaction(readOnly: true) {
            Pager pager = new Pager(params)
            DetachedCriteria mangoCriteria =  buildCriteria(params, closure)
            mangoCriteria.list(max: pager.max, offset: pager.offset)
        }
    }

    /**
     *  Calculates sums for specified properties in enities query restricted by mango criteria
     *
     * @param params mango language criteria map
     * @param sums query of properties names that sums should be calculated for
     * @param closure additional restriction for criteria
     * @return map where keys are names of fields and value - sum for restricted entities
     */
    @CompileDynamic
    Map countTotals(Map params = [:], List<String> sums,  Closure closure = null) {
        DetachedCriteria mangoCriteria =  buildCriteria(params, closure)

        List totalList
        withTransaction(readOnly: true) {
            totalList = mangoCriteria.list{
                projections {
                    for(String sumField: sums){
                        sum(sumField)
                    }
                }
            }
        }

        Map result = [:]
        sums.eachWithIndex{String name, i->
            result[name] = totalList[0][i]
        }
        return result
    }

    DomainException handleException(D entity, RuntimeException e) {
        return DaoUtil.handleException(entity, e)
    }

    public <T> T withTransaction(Map transProps = [:], Closure<T> callable) {
        GormEnhancer.findStaticApi(getDomainClass()).withTransaction(transProps, callable)
    }

//    public <T> T withTransaction(Closure<T> callable) {
//        GormEnhancer.findStaticApi(getDomainClass()).withTransaction(callable)
//    }

}
