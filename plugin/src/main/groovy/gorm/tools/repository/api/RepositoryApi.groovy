package gorm.tools.repository.api

import gorm.tools.repository.errors.DomainException
import gorm.tools.databinding.MapBinder
import grails.validation.ValidationException
import groovy.transform.CompileStatic
import org.springframework.dao.DataAccessException

/**
 * A complete interface for the Repository.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@CompileStatic
interface RepositoryApi<D> {

    /**
     * The gorm domain class. will generally get set in contructor or using the generic as
     * done in {@link gorm.tools.repository.GormRepo#getDomainClass}
     * using the {@link org.springframework.core.GenericTypeResolver}
     */
    Class<D> getDomainClass()

    /** The data binder to use. By default gets injected with EntityMapBinder*/
    MapBinder getMapBinder()
    /** default to true. If false only method events are invoked on the implemented Repository. */
    boolean isEnableEvents()

    /**
     * Transactional wrap for {@link #doPersist}
     */
    D persist(D entity, Map args)

    /**
     * Transactional wrap for {@link #doPersist}
     */
    D persist(D entity)

    /**
     * saves a domain entity with the passed in args.
     * If a {@link ValidationException} is caught it wraps and throws it with our DataValidationException.
     *
     * @param entity the domain entity to call save on
     * @param args the arguments to pass to save
     * @throws DataAccessException if a validation or DataAccessException error happens
     */
    D doPersist(D entity, Map args)

    /**
     * Transactional wrap for {@link #doCreate}
     */
    D create(Map data)

    /**
     * Creates entity using the data from params. calls the {@link #bindAndSave} with bindMethod='Create'
     *
     * @param data the data to bind onto the entity
     * @return the created domain entity
     * @see #doPersist
     */
    D doCreate(Map data, Map args)

    /**
     * Transactional wrap for {@link #doUpdate}
     */
    D update(Map data)

    /**
     * Updates entity using the data from params. calls the {@link #bind} with bindMethod='Update'
     *
     * @param data the data to bind onto the entity
     * @return the updated domain entity
     * @see #doPersist
     */
    D doUpdate(Map data, Map args)

    /**
     * use {@link #getMapBinder} and bind the data using the bindMethod
     *
     * @param entity
     * @param row
     * @param bindMethod
     */
    void bind(D entity, Map data, String bindMethod)

    /**
     * Deletes a new domain entity base on the id in the params.
     *
     * @param params the parameter map that has the id for the domain entity to delete
     * @throws gorm.tools.repository.errors.DomainException if its not found or if a DataIntegrityViolationException is thrown
     */
    void removeById(Serializable id, Map args)

    void removeById(Serializable id)

    /**
     * Calls delete always with flush = true so we can intercept any DataIntegrityViolationExceptions.
     *
     * @param entity the domain entity
     */
    void remove(D entity)

    void doRemove(D entity, Map args)

    /**
     * gets and verifies that the entity can eb retrieved and version matches.
     *
     * @param id required, the id to get
     * @param version - can be null. if its passed in then it validates its that same as the version in the retrieved entity.
     * @return the retrieved entity. Will always be an entity as this throws an error if not
     *
     * @throws gorm.tools.repository.errors.DomainNotFoundException if its not found
     * @throws org.springframework.dao.OptimisticLockingFailureException if the versions mismatch
     */
    D get(Serializable id, Long version)

    /**
     * calls {@link #get(Serializable id, Long version)}
     *
     * @param params expects a Map with an [id] key and optionally a [version] key
     */
    D get(Map<String, Object> params)

    DomainException handleException(D entity, RuntimeException e)

    void batchCreate(Map args, List<Map> batch)

    void batchCreate(List<Map> batch)

    void batchUpdate(Map args, List<Map> batch)

    void batchUpdate(List<Map> batch)

    void batchRemove(Map args, List batch)

    void batchRemove(List batch)

    void batchPersist(Map args, List<D> list)

    void batchPersist(List<D> list)
}
