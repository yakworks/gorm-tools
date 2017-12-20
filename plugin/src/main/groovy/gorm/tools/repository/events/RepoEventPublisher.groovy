package gorm.tools.repository.events

import gorm.tools.databinding.BindAction
import gorm.tools.repository.api.RepositoryApi
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.events.EventPublisher
import grails.plugin.gormtools.RepositoryArtefactHandler
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
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

        findAndCacheEventMethods(RepositoryEventType.BeforeBind.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.AfterBind.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.BeforeRemove.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.AfterRemove.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.BeforePersist.eventKey, repoClass, events)
        findAndCacheEventMethods(RepositoryEventType.AfterPersist.eventKey, repoClass, events)
    }

    private void findAndCacheEventMethods(String eventKey, Class repoClass, Map<String, Method> events) {
        Method method = ReflectionUtils.findMethod(repoClass, eventKey, null)
        if (method != null) events[eventKey] = method
    }

    void publishEvents(RepositoryApi repo, RepositoryEvent event, Object... args) {
        invokeEventMethod(repo, event.eventKey, args)
        if (!repo.enableEvents) return
        applicationEventPublisher.publishEvent(event)
        //println event.routingKey
        notify(event.routingKey, event)
    }

    void doBeforePersist(RepositoryApi repo, GormEntity entity, Map args) {
        BeforePersistEvent event = new BeforePersistEvent(repo, entity, args)
        publishEvents(repo, event, [entity, args] as Object[])
    }

    void doAfterPersist(RepositoryApi repo, GormEntity entity, Map args) {
        AfterPersistEvent event = new AfterPersistEvent(repo, entity, args)
        publishEvents(repo, event, [entity, args] as Object[])
    }

    void doBeforeBind(RepositoryApi repo, GormEntity entity, Map data, BindAction bindAction) {
        BeforeBindEvent event = new BeforeBindEvent(repo, entity, data, bindAction.name())
        publishEvents(repo, event, [entity, data, bindAction] as Object[])
    }

    void doAfterBind(RepositoryApi repo, GormEntity entity, Map data, BindAction bindAction) {
        AfterBindEvent event = new AfterBindEvent(repo, entity, data, bindAction.name())
        publishEvents(repo, event, [entity, data, bindAction] as Object[])
    }

    void doBeforeRemove(RepositoryApi repo, GormEntity entity) {
        BeforeRemoveEvent event = new BeforeRemoveEvent(repo, entity)
        publishEvents(repo, event, [entity] as Object[])
    }

    void doAfterRemove(RepositoryApi repo, GormEntity entity) {
        AfterRemoveEvent event = new AfterRemoveEvent(repo, entity)
        publishEvents(repo, event, [entity] as Object[])
    }
}
