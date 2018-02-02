package gorm.tools.repository.api

import gorm.tools.databinding.BindAction
import gorm.tools.databinding.MapBinder
import grails.validation.ValidationException
import org.springframework.dao.DataAccessException

/**
 * A complete interface for the Repository.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@SuppressWarnings("MethodCount")
interface RepositoryApi<D> {

    /**
     * The java class for the Gorm domain (persistence entity). will generally get set in constructor or using the generic as
     * done in {@link gorm.tools.repository.GormRepo#getEntityClass}
     * using the {@link org.springframework.core.GenericTypeResolver}
     * @see org.grails.datastore.mapping.model.PersistentEntity#getJavaClass().
     */
    Class<D> getEntityClass()

    /** The data binder to use. By default gets injected with EntityMapBinder*/
    MapBinder getMapBinder()

    /** default to true. If false only method events are invoked on the implemented Repository. */
    Boolean getEnableEvents()

    /**
     * Transactional wrap for {@link #doPersist} with arguments to pass to save
     */
    D persist(Map args, D entity)

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
    D doPersist(Map args, D entity)

    /**
     * Transactional wrap for {@link #doCreate}
     */
    D create(Map data)
    D create(Map args, Map data)

    /**
     * Creates entity using the data from params. calls the {@link #bind} with BindAction.Create
     *
     * @param data the data to bind onto the entity
     * @return the created domain entity
     * @see #doPersist
     */
    D doCreate(Map args, Map data)

    /**
     * Transactional wrap for {@link #doUpdate}
     */
    D update(Map data)
    D update(Map args, Map data)

    /**
     * Updates entity using the data from params. calls the {@link #bind} with BindAction.Update
     *
     * @param data the data to bind onto the entity
     * @return the updated domain entity
     * @see #doPersist
     */
    D doUpdate(Map args, Map data)

    /**
     * binds by calling {@link #doBind} and fires before and after events
     * better to override doBind in implementing classes for custom logic.
     * Or just implement the beforeBind|afterBind event methods
     */
    void bind(Map args, D entity, Map data, BindAction bindAction)

    /**
     * Main bind method that redirects call to the injected mapBinder. see {@link #getMapBinder}
     * override this one in implementing classes. can also call this if you don't want events to fire
     */
    void doBind(Map args, D entity, Map data, BindAction bindAction)

    /**
     * Remove by ID. Wrapping this in a Transaction in an implementing class here is optional
     *
     * @param id - the id for the
     * @param args - the args to pass to delete. can be null and [flush] being the most common
     *
     * @throws gorm.tools.repository.errors.EntityValidationException if its not found or if a DataIntegrityViolationException is thrown
     */
    void removeById(Map args, Serializable id)
    void removeById(Serializable id)

    /**
     * Wrappep in Trx. Calls doRemove.
     *
     * @param entity the domain entity
     */
    void remove(D entity)
    void remove(Map args, D entity)

    void doRemove(Map args, D entity)
    void doRemove(D entity)

    /**
     * gets and verifies that the entity can be retrieved and version matches.
     *
     * @param id required, the id to get
     * @param version - can be null. if its passed in then it validates its that same as the version in the retrieved entity.
     * @return the retrieved entity. Will always be an entity as this throws an error if not
     *
     * @throws gorm.tools.repository.errors.EntityNotFoundException if its not found
     * @throws org.springframework.dao.OptimisticLockingFailureException if the versions mismatch
     */
    D get(Serializable id, Long version)

    /**
     * This default will redirect the call to {@link #get(Serializable id, Long version)}.
     * Implementing classes can override this and add custom finders
     * using another unique lookup key other than id, such as customer number or invoice number. Unlike the normal get(id)
     * This throws a EntityNotFoundException if nothing is found instead of returning a null.
     *
     * @param args - name params expects at least and [id] key and optionally a version,
     *    implementation classes can customize to work with more.
     *
     * @return the entity. Won't return null, instead it throws an exception
     */
    D get(Map params)

    RuntimeException handleException(RuntimeException e, D entity)

    void batchCreate(Map args, List<Map> batch)

    void batchCreate(List<Map> batch)

    void batchUpdate(Map args, List<Map> batch)

    void batchUpdate(List<Map> batch)

    void batchRemove(Map args, List batch)

    void batchRemove(List batch)

    void batchPersist(Map args, List<D> list)

    void batchPersist(List<D> list)
}
