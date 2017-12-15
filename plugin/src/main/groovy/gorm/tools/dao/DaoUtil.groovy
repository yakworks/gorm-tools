package gorm.tools.dao

import gorm.tools.dao.errors.DomainException
import gorm.tools.dao.errors.DomainNotFoundException
import grails.util.GrailsNameUtils
import grails.validation.ValidationException
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.interceptor.TransactionAspectSupport

/**
 * A bunch of statics to support the GormDaoSupport.
 * this is also setup as daoUtilsBean so that it gets injected with the ApplicationContext once its setup
 */
@CompileStatic
class DaoUtil implements ApplicationContextAware {

    static ApplicationContext ctx
    static ApplicationEventPublisher applicationEventPublisher

    void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx
        applicationEventPublisher = (ApplicationEventPublisher) ctx
    }

    static String getDaoClassName(Class domainClass) {
        return "${domainClass.name}Dao"
    }

    static String getDaoBeanName(Class domainClass) {
        return "${GrailsNameUtils.getPropertyName(domainClass.name)}Dao"
    }

    static DaoApi findDao(Class domainClass) {
        return ctx.getBean(getDaoBeanName(domainClass), DaoApi)
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
                Map msgMap = DaoMessage.optimisticLockingFailure(entity)
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
            throw new DomainNotFoundException(DaoMessage.notFound(domainClassName, params))
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
    static void flushAndClear() {
        flush()
        clear()
    }

    /**
     * flushes the session
     */
    @CompileDynamic
    static void flush() {
        ctx.sessionFactory?.currentSession?.flush()
    }

    /**
     * clears the session cache
     */
    @CompileDynamic
    static void clear() {
        ctx.sessionFactory?.currentSession?.clear()
    }

    static DomainException handleException(GormEntity entity, RuntimeException ex) throws DataAccessException {
        if (ex instanceof ValidationException) {
            if (ex instanceof DomainException) return (DomainException) ex //if this is already fired

            ValidationException ve = (ValidationException) ex
            return new DomainException(DaoMessage.notSaved(entity), entity, ve.errors, ve)
        } else if (ex instanceof DataIntegrityViolationException) {
            String ident = DaoMessage.badge(entity.ident(), entity)
            //log.error("dao delete error on ${entity.id} of ${entity.class.name}",dae)
            return new DomainException(DaoMessage.notDeleted(entity, ident), entity, ex)
        } else if (ex instanceof DataAccessException) {
            //log.error("unexpected dao save error on ${entity.id} of ${entity.class.name}",dae)
            //TODO we can build a better message with optimisticLockingFailure(entity) if dae.cause instanceof org.springframework.dao.OptimisticLockingFailureException
            //TODO also, in the case of optimisticLocking, is that really un expected? shoud we log it?
            //TODO we shold really chnage the message from the default notSaved as this is more of a critical low level error a
            //and save the default notSaved for when a validation occurs like above
            return new DomainException(DaoMessage.notSaved(entity), entity, ex) //make a DaoMessage.notSavedDataAccess
        }

    }

}

