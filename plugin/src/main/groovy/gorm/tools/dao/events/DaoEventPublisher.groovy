package gorm.tools.dao.events

import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.events.EventPublisher
import grails.plugin.dao.DaoArtefactHandler
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.core.Datastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.util.ReflectionUtils

import javax.annotation.PostConstruct
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Invokes event methods on Dao classes.
 */
@CompileStatic
class DaoEventPublisher implements EventPublisher {
    @Autowired
    GrailsApplication grailsApplication

    ApplicationEventPublisher applicationEventPublisher

    private final Map<String, Map<String, Method>> eventsCache = new ConcurrentHashMap<>()

    @PostConstruct
    void init() {
        applicationEventPublisher = (ApplicationEventPublisher) grailsApplication.mainContext

        GrailsClass[] daoClasses = grailsApplication.getArtefacts(DaoArtefactHandler.TYPE)
        for (GrailsClass daoClass : daoClasses) {
            cacheEventsMethods(daoClass.clazz)
        }
    }

    public void invokeEventMethod(Object dao, String eventKey, Object... args) {
        Map<String, Method> events = eventsCache.get(dao.class.simpleName)
        if (!events) return

        Method method = events.get(eventKey)
        if (!method) return

        ReflectionUtils.invokeMethod(method, dao, args)
    }

    void cacheEventsMethods(Class daoClass) {
        Map<String, Method> events = new ConcurrentHashMap<>()
        eventsCache.put(daoClass.simpleName, events)

        findAndCacheEventMethods(DaoEventType.BeforeCreate.eventKey, daoClass, events)
        findAndCacheEventMethods(DaoEventType.AfterCreate.eventKey, daoClass, events)
        findAndCacheEventMethods(DaoEventType.BeforeUpdate.eventKey, daoClass, events)
        findAndCacheEventMethods(DaoEventType.AfterUpdate.eventKey, daoClass, events)
        findAndCacheEventMethods(DaoEventType.BeforeRemove.eventKey, daoClass, events)
        findAndCacheEventMethods(DaoEventType.AfterRemove.eventKey, daoClass, events)
        findAndCacheEventMethods(DaoEventType.BeforePersist.eventKey, daoClass, events)
        findAndCacheEventMethods(DaoEventType.AfterPersist.eventKey, daoClass, events)
    }

    private void findAndCacheEventMethods(String eventKey, Class daoClass, Map<String, Method> events) {
        Method method = ReflectionUtils.findMethod(daoClass, eventKey, null)
        if (method != null) events[eventKey] = method
    }

    Datastore getDatastore(entity) {
        GormEnhancer.findInstanceApi(entity.class).datastore
    }

    void publishEvent(DaoEvent event) {
        applicationEventPublisher.publishEvent(event)
        //println event.routingKey
        notify(event.routingKey, event)
    }

    void doBeforePersist(Object dao, GormEntity entity) {
        BeforePersistEvent event = new BeforePersistEvent(getDatastore(entity), entity)
        invokeEventMethod(dao, event.eventKey, [entity] as Object[])
        publishEvent(event)
    }

    void doAfterPersist(Object dao, GormEntity entity) {
        AfterPersistEvent event = new AfterPersistEvent(getDatastore(entity), entity)
        invokeEventMethod(dao, event.eventKey, [entity] as Object[])
        publishEvent(event)
    }

    void doBeforeCreate(Object dao, GormEntity entity, Map params) {
        BeforeCreateEvent event = new BeforeCreateEvent(getDatastore(entity), entity, params)
        invokeEventMethod(dao, event.eventKey, [entity, params] as Object[])
        publishEvent(event)
    }

    void doAfterCreate(Object dao, GormEntity entity, Map params) {
        AfterCreateEvent event = new AfterCreateEvent(getDatastore(entity), entity, params)
        invokeEventMethod(dao, event.eventKey, [entity, params] as Object[])
        publishEvent(event)
    }

    void doBeforeUpdate(Object dao, GormEntity entity, Map params) {
        BeforeUpdateEvent event = new BeforeUpdateEvent(getDatastore(entity), entity, params)
        invokeEventMethod(dao, event.eventKey, [entity, params] as Object[])
        publishEvent(event)
    }

    void doAfterUpdate(Object dao, GormEntity entity, Map params) {
        AfterUpdateEvent event = new AfterUpdateEvent(getDatastore(entity), entity, params)
        invokeEventMethod(dao, event.eventKey, [entity, params] as Object[])
        publishEvent(event)
    }

    void doBeforeRemove(Object dao, GormEntity entity) {
        BeforeRemoveEvent event = new BeforeRemoveEvent(getDatastore(entity), entity)
        invokeEventMethod(dao, event.eventKey, [entity] as Object[])
        publishEvent(event)
    }

    void doAfterRemove(Object dao, GormEntity entity) {
        AfterRemoveEvent event = new AfterRemoveEvent(getDatastore(entity), entity)
        invokeEventMethod(dao, event.eventKey, [entity] as Object[])
        publishEvent(event)
    }
}
