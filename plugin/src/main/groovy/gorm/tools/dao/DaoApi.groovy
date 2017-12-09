package gorm.tools.dao

import gorm.tools.dao.errors.DomainException
import groovy.transform.CompileStatic

@CompileStatic
interface DaoApi<D> {

    Class<D> getDomainClass()

    /**
     * Saves a domain entity with the passed in args and rewraps ValidationException with DomainException on error.
     *
     * @param entity the domain entity to call save on
     * @param args the arguments to pass to save
     * @throws gorm.tools.dao.errors.DomainException if a validation or DataAccessException error happens
     */
    D persist(D entity, Map args)

    /**
     * Saves a domain entity.
     * rewraps ValidationException in DomainException on error.
     *
     * @param entity the domain entity to call save on
     * @throws gorm.tools.dao.errors.DomainException if a validation or DataAccessException error happens
     */
    D persist(D entity)

    D create(Map params)

    D doCreate(Map params)

    //D batchCreate(List<Map> rows)

    D update(Map params)

    //TODO
    //D batchUpdate(List<Map> rows)

    D bindAndSave(D entity, Map params, String bindMethod)

    void bind(D entity, Map row, String bindMethod)

    /**
     * Deletes a new domain entity base on the id in the params.
     *
     * @param params the parameter map that has the id for the domain entity to delete
     * @throws gorm.tools.dao.errors.DomainException if its not found or if a DataIntegrityViolationException is thrown
     */
    void removeById(Serializable id)

    /**
     * Calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions.
     *
     * @param entity the domain entity
     * @throws gorm.tools.dao.errors.DomainException if a spring DataIntegrityViolationException is thrown
     */
    void remove(D entity)

    /**
     * gets and verfiies that the enity can eb retireved and version matches.
     *
     * @param id required, the id to get
     * @param version - can be null. if its passed in then it validates its that same as the version in the retrieved entity.
     * @return the retrieved entity. Will always be an entity as this throws an error if not
     *
     * @throws gorm.tools.dao.errors.DomainNotFoundException if its not found
     * @throws gorm.tools.dao.errors.DomainException if the versions mismatch
     */
    D get(Serializable id, Long version)

    /**
     * calls {@link #get(Serializable id, Long version)}
     *
     * @param params expects a Map with an id key and optionally a version
     * @return
     */
    D get(Map params)

    DomainException handleException(D entity, RuntimeException e)
}
