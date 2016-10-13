package grails.plugin.dao

import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.transaction.interceptor.TransactionAspectSupport

/**
* A bunch of statics to support the GormDaoSupport.
* this is also setup as daoUtilsBean so that it gets injected with the ApplicationContext once its setup
*/
class DaoUtil implements ApplicationContextAware 
{
	
	static ApplicationContext ctx

	public void setApplicationContext(ApplicationContext ctx) throws BeansException { 
		this.ctx = ctx
	}
	
	/**
	* checks the passed in version with the version on the entity (entity.version)
	* make sure entity.version is not greater
	*
	* @param  entity  the domain object the check
	* @param  ver the version this used to be (entity will have the )
	* @throws DomainException adds a rejectvalue to the errors on the entity and throws with code optimistic.locking.failure
	*/
	static void checkVersion(entity,ver){
		if (ver == null) return
		def version = ver.toLong()
		if (entity.version > version) {
			def msgMap = DaoMessage.optimisticLockingFailure(entity)
			entity.errors.rejectValue("version", msgMap.code, msgMap.args as Object[],msgMap.defaultMessage)
			throw new DomainException(msgMap, entity, entity.errors)
		}
	}

	/**
	* check that the passed in entity is not null and throws DomainException setup with the notfound message
	*
	* @param  entity  the domain object the check
	* @param  params  the params map
	* @param  domainClassName  the name of the domain
	* @throws DomainException if it not found
	*/
	static void checkFound(entity, Map params,String domainClassName){
		if (!entity) {
			throw new DomainException(DaoMessage.notFound(domainClassName,params), null)
		}
	}

	/** 
	* force a roll back if in a transaction
	*/
	static void rollback(){
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
	}

	/** 
	* this is more or less copied from grails ClosureEventTriggeringInterceptor
	* it fires the passed in event, first trying a method and then a closure if it exists
	*
	* @param  dao  theobject you want to call the event on
	* @param  event  the method event name to look for
	* @param  entity  the domain entity this is for
	* @param params the params to pass to the event method
	*/
	static boolean triggerEvent(dao, String event, entity, params) {
		def result = false
		def eventTriggered = false
		if(dao.respondsTo(event,Object) || dao.respondsTo(event,Object,Object) ) {
			eventTriggered = true
			if(params){
				result = dao."$event"(entity,params)
			}else{
				result = dao."$event"(entity)
			}
			if(result instanceof Boolean) result = !result
			else {
				result = false
			}
		}
		else if(dao.hasProperty(event)) {
			eventTriggered = true
			def callable = dao."$event"
			if(callable instanceof Closure) {
				callable.resolveStrategy = Closure.DELEGATE_FIRST
				callable.delegate = dao
				if(params){
					result = callable.call(entity,params)
				}else{
					result = callable.call(entity)
				}

				if(result instanceof Boolean) result = !result
				else {
					result = false
				}
			}
		}
		return result
	}

	/** 
	* flushes the session and clears the session cache and the DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
	*/
	static void flushAndClear(){
		flush()
		clear()
	}
	
	/** 
	* flushes the session 
	*/
	static void flush(){
		ctx.sessionFactory.currentSession.flush()
	}
	
	/** 
	* clears the session cache and the DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
	*/
	static void clear(){
		ctx.sessionFactory.currentSession.clear()
		DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP.get().clear()
	}

}

