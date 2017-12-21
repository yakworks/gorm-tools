package gorm.tools.repository.events

import gorm.tools.databinding.BindAction
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.api.RepositoryApi
import grails.core.GrailsApplication
import grails.events.bus.EventBus
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
 * Invokes "event methods" on Repository artifacts as well as publish spring events for @EventListeners
 * which can be Transactional and fires Grails events that will be asynchronous
 */
@CompileStatic
class RepoEventPublisher {
    @Autowired
    private GrailsApplication grailsApplication

    @Autowired
    private EventBus eventBus

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher

    private final Map<String, Map<String, Method>> repoEventMethodCache = new ConcurrentHashMap<>()

    @PostConstruct
    void init() {
        for (Class repoClass : grailsApplication.getArtefacts(RepositoryArtefactHandler.TYPE)*.clazz) {
            cacheEventsMethods(repoClass)
        }
    }

    void cacheEventsMethods(Class repoClass) {
        Map<String, Method> eventMethodMap = new ConcurrentHashMap<>()
        repoEventMethodCache.put(repoClass.simpleName, eventMethodMap)

        findAndCacheEventMethods(RepositoryEventType.BeforeBind.eventKey, repoClass, eventMethodMap)
        findAndCacheEventMethods(RepositoryEventType.AfterBind.eventKey, repoClass, eventMethodMap)
        findAndCacheEventMethods(RepositoryEventType.BeforeRemove.eventKey, repoClass, eventMethodMap)
        findAndCacheEventMethods(RepositoryEventType.AfterRemove.eventKey, repoClass, eventMethodMap)
        findAndCacheEventMethods(RepositoryEventType.BeforePersist.eventKey, repoClass, eventMethodMap)
        findAndCacheEventMethods(RepositoryEventType.AfterPersist.eventKey, repoClass, eventMethodMap)

    }

    private void findAndCacheEventMethods(String eventKey, Class repoClass, Map<String, Method> events) {
        Method method = ReflectionUtils.findMethod(repoClass, eventKey, null)
        if (method != null) events[eventKey] = method
    }

    void publishEvents(RepositoryApi repo, RepositoryEvent event, Object... args) {
        invokeEventMethod(repo, event.eventKey, args)
        if (!repo.enableEvents) return
        applicationEventPublisher.publishEvent(event)
        eventBus.notify(event.routingKey, event)
    }

    void invokeEventMethod(Object repo, String eventKey, Object... args) {
        Map<String, Method> eventMethodMap = repoEventMethodCache.get(repo.class.simpleName)
        //if (!eventMethodMap) return //eventMethodMap = cacheEventsMethods(repo.class)
        Method method = eventMethodMap?.get(eventKey)
        if (!method) return

        ReflectionUtils.invokeMethod(method, repo, args)
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

    void doBeforeRemove(RepositoryApi repo, GormEntity entity, Map args) {
        BeforeRemoveEvent event = new BeforeRemoveEvent(repo, entity)
        publishEvents(repo, event, [entity, args] as Object[])
    }

    void doAfterRemove(RepositoryApi repo, GormEntity entity, Map args) {
        AfterRemoveEvent event = new AfterRemoveEvent(repo, entity)
        publishEvents(repo, event, [entity, args] as Object[])

    }
}
