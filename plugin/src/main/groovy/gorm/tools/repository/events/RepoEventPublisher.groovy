package gorm.tools.repository.events

import gorm.tools.repository.api.RepositoryApi
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.events.EventPublisher
import grails.plugin.gormtools.RepositoryArtefactHandler
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
 * Invokes event methods on Repository artifcats.
 */
@CompileStatic
class RepoEventPublisher implements EventPublisher {
    @Autowired
    GrailsApplication grailsApplication

    ApplicationEventPublisher applicationEventPublisher

    private final Map<String, Map<String, Method>> eventsCache = new ConcurrentHashMap<>()

    @PostConstruct
    void init() {
        applicationEventPublisher = (ApplicationEventPublisher) grailsApplication.mainContext

        GrailsClass[] repoClasses = grailsApplication.getArtefacts(RepositoryArtefactHandler.TYPE)
        for (GrailsClass repoClass : repoClasses) {
            cacheEventsMethods(repoClass.clazz)
        }
    }

    public void invokeEventMethod(Object repo, String eventKey, Object... args) {
        Map<String, Method> events = eventsCache.get(repo.class.simpleName)
        if (!events) return

        Method method = events.get(eventKey)
        if (!method) return

        ReflectionUtils.invokeMethod(method, repo, args)
    }

    void cacheEventsMethods(Class repoClass) {
        Map<String, Method> events = new ConcurrentHashMap<>()
        eventsCache.put(repoClass.simpleName, events)

        findAndCacheEventMethods(RepositoryEventType.BeforeCreate.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.AfterCreate.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.BeforeUpdate.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.AfterUpdate.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.BeforeRemove.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.AfterRemove.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.BeforePersist.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.AfterPersist.eventKey, repoClass, events)
    }

    private void findAndCacheEventMethods(String eventKey, Class repoClass, Map<String, Method> events) {
        Method method = ReflectionUtils.findMethod(repoClass, eventKey, null)
        if (method != null) events[eventKey] = method
    }

    Datastore getDatastore(entity) {
        GormEnhancer.findInstanceApi(entity.class).datastore
    }

    void publishEvents(RepositoryApi repo, RepositoryEvent event, Object... args) {
        invokeEventMethod(repo, event.eventKey, args)
        if (!repo.enableEvents) return
        applicationEventPublisher.publishEvent(event)
        //println event.routingKey
        notify(event.routingKey, event)
    }

    void doBeforePersist(RepositoryApi repo, GormEntity entity, Map args) {
        BeforePersistEvent event = new BeforePersistEvent(getDatastore(entity), entity)
        publishEvents(repo, event, [entity, args] as Object[])
    }

    void doAfterPersist(RepositoryApi repo, GormEntity entity, Map args) {
        AfterPersistEvent event = new AfterPersistEvent(getDatastore(entity), entity)
        publishEvents(repo, event, [entity, args] as Object[])
    }

    void doBeforeCreate(RepositoryApi repo, GormEntity entity, Map params) {
        BeforeCreateEvent event = new BeforeCreateEvent(getDatastore(entity), entity, params)
        publishEvents(repo, event, [entity, params] as Object[])
    }

    void doAfterCreate(RepositoryApi repo, GormEntity entity, Map params) {
        AfterCreateEvent event = new AfterCreateEvent(getDatastore(entity), entity, params)
        publishEvents(repo, event, [entity, params] as Object[])
    }

    void doBeforeUpdate(RepositoryApi repo, GormEntity entity, Map params) {
        BeforeUpdateEvent event = new BeforeUpdateEvent(getDatastore(entity), entity, params)
        publishEvents(repo, event, [entity, params] as Object[])
    }

    void doAfterUpdate(RepositoryApi repo, GormEntity entity, Map params) {
        AfterUpdateEvent event = new AfterUpdateEvent(getDatastore(entity), entity, params)
        publishEvents(repo, event, [entity, params] as Object[])
    }

    void doBeforeRemove(RepositoryApi repo, GormEntity entity) {
        BeforeRemoveEvent event = new BeforeRemoveEvent(getDatastore(entity), entity)
        publishEvents(repo, event, [entity] as Object[])
    }

    void doAfterRemove(RepositoryApi repo, GormEntity entity) {
        AfterRemoveEvent event = new AfterRemoveEvent(getDatastore(entity), entity)
        publishEvents(repo, event, [entity] as Object[])
    }
}
