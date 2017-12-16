package gorm.tools.dao.events

import gorm.tools.dao.DaoApi
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

    void publishEvents(DaoApi dao, DaoEvent event, Object... args) {
        invokeEventMethod(dao, event.eventKey, args)
        if (!dao.enableEvents) return
        applicationEventPublisher.publishEvent(event)
        //println event.routingKey
        notify(event.routingKey, event)
    }

    void doBeforePersist(DaoApi dao, GormEntity entity, Map args) {
        BeforePersistEvent event = new BeforePersistEvent(getDatastore(entity), entity)
        publishEvents(dao, event, [entity, args] as Object[])
    }

    void doAfterPersist(DaoApi dao, GormEntity entity, Map args) {
        AfterPersistEvent event = new AfterPersistEvent(getDatastore(entity), entity)
        publishEvents(dao, event, [entity, args] as Object[])
    }

    void doBeforeCreate(DaoApi dao, GormEntity entity, Map params) {
        BeforeCreateEvent event = new BeforeCreateEvent(getDatastore(entity), entity, params)
        publishEvents(dao, event, [entity, params] as Object[])
    }

    void doAfterCreate(DaoApi dao, GormEntity entity, Map params) {
        AfterCreateEvent event = new AfterCreateEvent(getDatastore(entity), entity, params)
        publishEvents(dao, event, [entity, params] as Object[])
    }

    void doBeforeUpdate(DaoApi dao, GormEntity entity, Map params) {
        BeforeUpdateEvent event = new BeforeUpdateEvent(getDatastore(entity), entity, params)
        publishEvents(dao, event, [entity, params] as Object[])
    }

    void doAfterUpdate(DaoApi dao, GormEntity entity, Map params) {
        AfterUpdateEvent event = new AfterUpdateEvent(getDatastore(entity), entity, params)
        publishEvents(dao, event, [entity, params] as Object[])
    }

    void doBeforeRemove(DaoApi dao, GormEntity entity) {
        BeforeRemoveEvent event = new BeforeRemoveEvent(getDatastore(entity), entity)
        publishEvents(dao, event, [entity] as Object[])
    }

    void doAfterRemove(DaoApi dao, GormEntity entity) {
        AfterRemoveEvent event = new AfterRemoveEvent(getDatastore(entity), entity)
        publishEvents(dao, event, [entity] as Object[])
    }
}
