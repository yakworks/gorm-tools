package grails.plugin.dao

import grails.core.GrailsApplication
import org.grails.plugins.domain.DomainClassGrailsPlugin
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.transaction.interceptor.TransactionAspectSupport
import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic

/**
 * A bunch of statics to support the GormDaoSupport.
 * this is also setup as daoUtilsBean so that it gets injected with the ApplicationContext once its setup
 */
@CompileStatic
class DaoUtil implements ApplicationContextAware {

	static ApplicationContext ctx

	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.ctx = ctx
	}

	/**
	 * checks the passed in version with the version on the entity (entity.version)
	 * make sure entity.version is not greater
	 *
	 * @param entity the domain object the check
	 * @param ver the version this used to be (entity will have the )
	 * @throws DomainException adds a rejectvalue to the errors on the entity and throws with code optimistic.locking.failure
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

}

