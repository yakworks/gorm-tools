package gorm.tools.repository

import gorm.tools.beans.AppCtx
import gorm.tools.repository.api.RepositoryApi
import gorm.tools.repository.errors.DomainException
import gorm.tools.repository.errors.DomainNotFoundException
import grails.plugin.gormtools.RepositoryArtefactHandler
import grails.validation.ValidationException
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.core.Datastore
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.interceptor.TransactionAspectSupport

/**
 * A bunch of statics to support the repositories.
 * this is also setup as repoUtilBean so that it gets injected with the ApplicationContext once its setup
 *
 * @author Joshua Burnett (@basejump)
 * @since 1.0
 */
@CompileStatic
class RepoUtil {

    static String getRepoClassName(Class domainClass) {
        RepositoryArtefactHandler.getRepoClassName(domainClass)
    }

    static String getRepoBeanName(Class domainClass) {
        RepositoryArtefactHandler.getRepoBeanName(domainClass)
    }

    static RepositoryApi findRepository(Class domainClass) {
        return AppCtx.get(getRepoBeanName(domainClass), RepositoryApi)
    }

    /**
     * checks the passed in version with the version on the entity (entity.version)
     * make sure entity.version is not greater
     *
     * @param entity the domain object the check
     * @param ver the version this used to be (entity will have the )
     * @throws DomainException adds a rejectvalue to the errors on the entity and throws with code optimistic.locking.failure
     */
    static void checkVersion(GormEntity entity, Long oldVersion) {
        if (oldVersion == null) return
        if (entity.hasProperty('version')) {
            Long currentVersion = entity['version'] as Long
            //println "currentVersion: $currentVersion  oldVersion: $oldVersion"
            if (currentVersion > oldVersion) {
                Map msgMap = RepoMessage.optimisticLockingFailure(entity)
                entity.errors.rejectValue("version", msgMap.code as String, msgMap.args as Object[], msgMap.defaultMessage as String)
                throw new DomainException(msgMap, entity, entity.errors)
            }
        }
    }

    /**
     * check that the passed in entity is not null and throws DomainException setup with the notfound message
     *
     * @param entity the domain object the check
     * @param params the params map
     * @param domainClassName the name of the domain
     * @throws DomainException if it not found
     */
    static void checkFound(entity, Map params, String domainClassName) {
        if (!entity) {
            throw new DomainNotFoundException(RepoMessage.notFound(domainClassName, params))
        }
    }

    /**
     * force a roll back if in a transaction
     */
    static void rollback() {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
    }

    /**
     * flushes the session and clears the session cache and the DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
     */
    @Deprecated
    static void flushAndClear() {
        flush()
        clear()
    }

    /**
     * flushes the session
     */
    @CompileDynamic
    @Deprecated
    static void flush() {
        Datastore ds = GormEnhancer.findSingleDatastore()
        if(ds.hasCurrentSession()) ds.getCurrentSession().flush()
    }

    /**
     * clears the session cache
     */
    @CompileDynamic
    @Deprecated
    static void clear() {
        Datastore ds = GormEnhancer.findSingleDatastore()
        if(ds.hasCurrentSession()) ds.getCurrentSession().clear()
    }

    @CompileDynamic
    void flushAndClear(TransactionStatus status) {
        //TransactionObject txObject = (status as DefaultTransactionStatus).transaction as TransactionObject
        status.flush()
        clear(status)
    }

    @CompileDynamic
    void clear(TransactionStatus status) {
        status.transaction.sessionHolder.getSession().clear()
    }

    static DomainException handleException(GormEntity entity, RuntimeException ex) throws DataAccessException {
        if (ex instanceof ValidationException) {
            if (ex instanceof DomainException) return (DomainException) ex //if this is already fired

            ValidationException ve = (ValidationException) ex
            return new DomainException(RepoMessage.notSaved(entity), entity, ve.errors, ve)
        } else if (ex instanceof DataIntegrityViolationException) {
            String ident = RepoMessage.badge(entity.ident(), entity)
            //log.error("repository delete error on ${entity.id} of ${entity.class.name}",dae)
            return new DomainException(RepoMessage.notDeleted(entity, ident), entity, ex)
        } else if (ex instanceof DataAccessException) {
            //log.error("unexpected repository save error on ${entity.id} of ${entity.class.name}",dae)
            //TODO we can build a better message with optimisticLockingFailure(entity) if dae.cause instanceof org.springframework.repository.OptimisticLockingFailureException
            //TODO also, in the case of optimisticLocking, is that really un expected? shoud we log it?
            //TODO we shold really chnage the message from the default notSaved as this is more of a critical low level error a
            //and save the default notSaved for when a validation occurs like above
            return new DomainException(RepoMessage.notSaved(entity), entity, ex) //make a RepoMessage.notSavedDataAccess
        }

    }

}

