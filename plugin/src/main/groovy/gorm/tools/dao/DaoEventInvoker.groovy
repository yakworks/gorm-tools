package gorm.tools.dao

import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugin.dao.DaoArtefactHandler
import grails.util.GrailsNameUtils
import groovy.transform.CompileStatic
import org.springframework.util.ReflectionUtils

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Invokes event methods on Dao classes.
 */
@CompileStatic
class DaoEventInvoker {
	GrailsApplication grailsApplication

	private final Map<String, Map<DaoEventType, Method>> eventsCache = new ConcurrentHashMap<>()

	DaoEventInvoker(GrailsApplication app) {
		this.grailsApplication = app
		GrailsClass[] daoClasses = grailsApplication.getArtefacts(DaoArtefactHandler.TYPE)
		for(GrailsClass daoClass : daoClasses) {
			cacheEvents(daoClass.clazz)
		}
	}

	public void invokeEvent(DaoEventType eventType, GormDao dao, Object... args) {
		Map<DaoEventType, Method> events = eventsCache.get(dao.class.simpleName)
		if(!events) return

		Method method = events.get(eventType)
		if(!method) return

		ReflectionUtils.invokeMethod(method, dao, args)
	}

	private void cacheEvents(Class daoClass) {
        Map<DaoEventType, Method> events = new ConcurrentHashMap<>()
		eventsCache.put(daoClass.simpleName, events)

		findAndCacheEvents(DaoEventType.BeforeCreate, daoClass, events)
		findAndCacheEvents(DaoEventType.AfterCreate, daoClass, events)
		findAndCacheEvents(DaoEventType.BeforeUpdate, daoClass, events)
		findAndCacheEvents(DaoEventType.AfterUpdate, daoClass, events)
		findAndCacheEvents(DaoEventType.BeforeRemove, daoClass, events)
		findAndCacheEvents(DaoEventType.AfterRemove, daoClass, events)
		findAndCacheEvents(DaoEventType.BeforePersist, daoClass, events)
		findAndCacheEvents(DaoEventType.AfterPersist, daoClass, events)
	}

	private void findAndCacheEvents(DaoEventType event, Class daoClass, Map<DaoEventType, Method> events) {
        Method method = ReflectionUtils.findMethod(daoClass, GrailsNameUtils.getPropertyNameRepresentation(event.name()))
		if(method != null) events[event] = method
	}
}
