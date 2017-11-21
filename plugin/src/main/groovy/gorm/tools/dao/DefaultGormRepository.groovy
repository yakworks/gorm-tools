package gorm.tools.dao

import grails.plugin.dao.DaoMessage
import grails.plugin.dao.DomainException
import grails.validation.ValidationException
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.gorm.GormStaticApi
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

@SuppressWarnings(['EmptyMethod', 'ReturnsNullInsteadOfEmptyCollection'])
@CompileStatic
class DefaultGormRepository<T, ID extends Serializable> implements GormRepository<T, ID> {

    boolean methodEvents = true

    private Class<T> thisDomainClass = T

    DefaultGormRepository() { }

    DefaultGormRepository(Class<T> clazz) {
        thisDomainClass = clazz
    }

    DefaultGormRepository(Class<T> clazz, boolean methodEvents) {
        thisDomainClass = clazz
        this.methodEvents = methodEvents
    }

    @Override
    Class<T> getDomainClass() {
        return thisDomainClass
    }
    void setDomainClass(Class<T> clazz) {
        thisDomainClass = clazz
    }

    /**
     * saves a domain entity with the passed in args and rewraps ValidationException with DomainException on error
     *
     * @param entity the domain entity instance to call save on
     * @param args the arguments to pass to the Gorm Domain.save(args)
     * @return the saved entity
     * @throws grails.plugin.dao.DomainException if a validation or DataAccessException error happens
     */
    @Override
    <S extends T> S save(S entity, Map args) {
        return doSave(entity, args)
    }

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity
     * @return the saved entity
     */
    @Override
    <S extends T> S save(S entity) {
        save(entity, [:])
    }

    protected final <S extends T> S doSave(S entity, Map args) {
        args['failOnError'] = true
        try {
            if (methodEvents) beforeSave(entity)
            return ((GormEntity)entity).save(args)
        }
        catch (ValidationException ve) {
            if (ve instanceof DomainException) throw ve //if this is already fired
            throw new DomainException(DaoMessage.notSaved(entity), entity, ve.errors, ve)
        }
        catch (DataAccessException dae) {
            //log.error("unexpected dao save error on ${entity.id} of ${entity.class.name}",dae)
            //TODO we can build a better message with optimisticLockingFailure(entity) if dae.cause instanceof org.springframework.dao.OptimisticLockingFailureException
            //TODO also, in the case of optimisticLocking, is that really un expected? shoud we log it?
            //TODO we shold really chnage the message from the default notSaved as this is more of a critical low level error a
            //and save the default notSaved for when a validation occurs like above
            throw new DomainException(DaoMessage.notSaved(entity), entity, dae) //make a DaoMessage.notSavedDataAccess
        }
    }

    /**
     * Saves all given entities.
     *
     * @param entities
     * @return the saved entities
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    @Override
    <S extends T> Iterable<S> save(Iterable<S> entities) {
        return null
    }

    /**
     * creates entity using the data from params and calls save for a new domain entity
     *
     * @param params the parameter map
     * @return the created entity
     * @throws DomainException if a validation error happens
     */
    @Override
    T create(Map params) {
        return null
    }

    /**
     * updates a new domain entity after binding data from params
     *
     * @param params the parameter map
     * @return the updated entity
     * @throws DomainException if a validation error happens or its not found with the params.id or the version is off and someone else edited it
     */
    @Override
    T update(Map params) {
        return null
    }

    /**
     * deletes a new domain entity base on the id in the params
     *
     * @param params the parameter map that has the id for the domain entity to delete
     * @throws DomainException if its not found or if a DataIntegrityViolationException is thrown
     */
    @Override
    Map remove(Map params) {
        return null
    }

    /**
     * Returns all entities sorted by the given options.
     *
     * @param sort
     * @return all entities sorted by the given options
     */
    @Override
    Iterable<T> findAll(Sort sort) {
        return null
    }

    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
     *
     * @param pageable
     * @return a page of entities
     */
    @Override
    Page<T> findAll(Pageable pageable) {
        return null
    }

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal null} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    @Override
    T findOne(ID id) {
        return null
    }

    /**
     * Returns whether an entity with the given id exists.
     *
     * @param id must not be {@literal null}.
     * @return true if an entity with the given id exists, {@literal false} otherwise
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    @Override
    boolean exists(ID id) {
        return false
    }

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    @Override
    List<T> findAll() {
        return currentGormStaticApi(thisDomainClass).list()
    }

    /**
     * Returns all instances of the type with the given IDs.
     *
     * @param ids
     * @return
     */
    @Override
    List<T> findAll(Iterable<ID> ids) {
        return null
    }

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities
     */
    @Override
    long count() {
        (Long)GormEnhancer.findStaticApi(domainClass).count()
    }

    /**
     * Deletes an instance from the datastore
     */
    @Override
    void delete(T entity, Map args) {
        doDelete(entity, args)
    }

    /**
     * Deletes the entity with the given id.
     *
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    @CompileDynamic
    @Override
    void delete(ID id) {
        delete((T)domainClass.load(id))
    }

    /**
     * calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions
     *
     * @param entity the domain entity
     * @throws DomainException if a spring DataIntegrityViolationException is thrown
     */
    @Override
    void delete(T entity) {
        doDelete(entity, [:])
    }

    /**
     * Deletes the given entities.
     *
     * @param entities
     * @throws IllegalArgumentException in case the given {@link Iterable} is {@literal null}.
     */
    @Override
    void delete(Iterable<? extends T> entities) {
        for (T entity : entities) {
            delete(entity)
        }
    }

    /**
     * Deletes all entities managed by the repository.
     * Not implemented by default
     */
    @Override
    void deleteAll() {
        throw new UnsupportedOperationException("not implemented")
    }

    protected final void doDelete(T entity, Map args) {
        if( !(args?.containsKey("flush")) ) args.flush = true
        try {
            if (methodEvents) beforeDelete(entity)
            ((GormEntity)entity).delete(args)
        }
        catch (DataIntegrityViolationException dae) {
            String ident = DaoMessage.badge(((GormEntity)entity).ident(), entity)
            //log.error("dao delete error on ${entity.id} of ${entity.class.name}",dae)
            throw new DomainException(DaoMessage.notDeleted(entity, ident), entity, dae)
        }
    }

    //event templates
    protected void beforeSave(T entity) { }

    protected void beforeDelete(T entity) { }

    protected void beforeInsertSave(T entity, Map params) { }

    protected void beforeUpdateSave(T entity, Map params) { }

    protected void beforeRemoveSave(T entity, Map params) { }

    private static GormStaticApi<T> currentGormStaticApi(Class<T> domainClass) {
        (GormStaticApi<T>)GormEnhancer.findStaticApi(domainClass)
    }

}
