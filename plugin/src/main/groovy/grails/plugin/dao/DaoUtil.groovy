package grails.plugin.dao

import gorm.tools.dao.DaoEventInvoker
import gorm.tools.dao.DaoEventType
import grails.plugin.dao.DaoMessage
import grails.plugin.dao.DomainException
import grails.plugin.dao.DomainNotFoundException
import grails.plugin.dao.GormDaoSupport
import grails.util.GrailsNameUtils
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.dao.DataAccessException
import org.springframework.transaction.interceptor.TransactionAspectSupport

/**
 * A bunch of statics to support the GormDaoSupport.
 * this is also setup as daoUtilsBean so that it gets injected with the ApplicationContext once its setup
 */
@CompileStatic
class DaoUtil implements ApplicationContextAware {

	static ApplicationContext ctx
    static DaoEventInvoker daoEventInvoker

	void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.ctx = ctx
	}

	/**
	 * checks the passed in version with the version on the entity (entity.version)
	 * make sure entity.version is not greater
	 *
	 * @param entity the domain object the check
	 * @param ver the version this used to be (entity will have the )
	 * @throws grails.plugin.dao.DomainException adds a rejectvalue to the errors on the entity and throws with code optimistic.locking.failure
	 */
	@CompileDynamic
	static void checkVersion(entity, ver) {
		if (ver == null) return
		Long version = ver.toLong()
		if (entity.version > version) {
			Map msgMap = DaoMessage.optimisticLockingFailure(entity)
			entity.errors.rejectValue("version", msgMap.code, msgMap.args as Object[], msgMap.defaultMessage)
			throw new DomainException(msgMap, entity, entity.errors)
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
		ctx.sessionFactory.currentSession.flush()
	}

	/**
	 * clears the session cache
	 */
	@CompileDynamic
	static void clear() {
		ctx.sessionFactory.currentSession.clear()
	}

    static void fireEvent(DaoEventType eventType, Object... args) { }
    static DataAccessException handleException(GormEntity entity, RuntimeException e) throws DataAccessException {
        return
    }

//	static GormDaoSupport getDao(Class entity) {
//		String domainName = entity.simpleName
//
//		String daoName = "${GrailsNameUtils.getPropertyName(domainName)}Dao"
//		GormDaoSupport dao
//		if (ctx.containsBean(daoName)) {
//			println "found $daoName"
//			println entity
//			dao = ctx.getBean(daoName) as GormDaoSupport
//		} else {
//			println "NOT found $daoName"
//			println entity
//			dao = (GormDaoSupport) ctx.getBean("gormDaoBean")
//			dao.domainClass = entity
//		}
//		return dao
//	}

}

