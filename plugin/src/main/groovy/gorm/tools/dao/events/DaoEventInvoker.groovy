package gorm.tools.dao.events

import gorm.tools.dao.GormDao
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugin.dao.DaoArtefactHandler
import grails.util.GrailsNameUtils
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.ReflectionUtils

import javax.annotation.PostConstruct
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Invokes event methods on Dao classes.
 */
@CompileStatic
class DaoEventInvoker {
    @Autowired
    GrailsApplication grailsApplication

    private final Map<String, Map<DaoEventType, Method>> eventsCache = new ConcurrentHashMap<>()

    @PostConstruct
    void init(){
        GrailsClass[] daoClasses = grailsApplication.getArtefacts(DaoArtefactHandler.TYPE)
        for(GrailsClass daoClass : daoClasses) {
            cacheEvents(daoClass.clazz)
        }
    }

    public void invokeEvent(GormDao dao, DaoEventType eventType, Object... args) {
        Map<DaoEventType, Method> events = eventsCache.get(dao.class.simpleName)
        if(!events) return

        Method method = events.get(eventType)
        if(!method) return

        ReflectionUtils.invokeMethod(method, dao, args)
    }

    void cacheEvents(Class daoClass) {
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
        Method method = ReflectionUtils.findMethod(daoClass, GrailsNameUtils.getPropertyNameRepresentation(event.name()), null)
        if(method != null) events[event] = method
    }
}
