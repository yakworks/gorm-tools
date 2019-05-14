/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.util.ReflectionUtils

import gorm.tools.databinding.BindAction
import gorm.tools.repository.api.RepositoryApi
import grails.core.GrailsApplication
import grails.events.bus.EventBus
import grails.plugin.gormtools.RepositoryArtefactHandler

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
    //private final Map<String, Map<String, Method>> repoListenerMethodCache = new ConcurrentHashMap<>()

    @PostConstruct
    void init() {
        for (Class repoClass : grailsApplication.getArtefacts(RepositoryArtefactHandler.TYPE)*.clazz) {
            cacheEventsMethods(repoClass)
        }
    }

    void cacheEventsMethods(Class repoClass) {
//        Map<String, Method> listenerMethodMap = new ConcurrentHashMap<>()
//        repoListenerMethodCache.put(repoClass.simpleName, listenerMethodMap)
//        findAndCacheListenerAnnotations(repoClass, listenerMethodMap)

        Map<String, Method> eventMethodMap = new ConcurrentHashMap<>()
        repoEventMethodCache.put(repoClass.simpleName, eventMethodMap)

        RepositoryEventType.values().each { RepositoryEventType et ->
            findAndCacheEventMethods(et.eventKey, repoClass, eventMethodMap)
        }
    }

    private void findAndCacheEventMethods(String eventKey, Class repoClass, Map<String, Method> events) {
        Method method = ReflectionUtils.findMethod(repoClass, eventKey, null)
        RepoListener ann = method?.getAnnotation(RepoListener)
        if (method != null && ann) events[eventKey] = method
    }

    void publishEvents(RepositoryApi repo, RepositoryEvent event, Object... methodArgs) {
        //invokeListenerMethod(repo, event)
        invokeEventMethod(repo, event.eventKey, methodArgs)
        if (!repo.enableEvents) return
        applicationEventPublisher.publishEvent(event)
        eventBus.notify(event.routingKey, event)
    }

//    void invokeListenerMethod(Object repo, RepositoryEvent event) {
//        Map<String, Method> listenerMap = repoListenerMethodCache.get(repo.class.simpleName)
//        Method method = listenerMap?.get(event.eventKey)
//        if (!method) return
//        ReflectionUtils.invokeMethod(method, repo, event)
//    }

    void invokeEventMethod(Object repo, String eventKey, Object... methodArgs) {
        Map<String, Method> eventMethodMap = repoEventMethodCache.get(repo.class.simpleName)
        //if (!eventMethodMap) return //eventMethodMap = cacheEventsMethods(repo.class)
        Method method = eventMethodMap?.get(eventKey)
        if (!method) return

        ReflectionUtils.invokeMethod(method, repo, methodArgs)
    }

    void doBeforePersist(RepositoryApi repo, GormEntity entity, Map args) {
        BeforePersistEvent event = new BeforePersistEvent(repo, entity, args)
        publishEvents(repo, event, [entity, event] as Object[])
    }

    void doAfterPersist(RepositoryApi repo, GormEntity entity, Map args) {
        AfterPersistEvent event = new AfterPersistEvent(repo, entity, args)
        publishEvents(repo, event, [entity, event] as Object[])
    }

    void doBeforeBind(RepositoryApi repo, GormEntity entity, Map data, BindAction bindAction, Map args) {
        BeforeBindEvent event = new BeforeBindEvent(repo, entity, data, bindAction, args)
        publishEvents(repo, event, [entity, data, event] as Object[])
    }

    void doAfterBind(RepositoryApi repo, GormEntity entity, Map data, BindAction bindAction, Map args) {
        AfterBindEvent event = new AfterBindEvent(repo, entity, data, bindAction, args)
        publishEvents(repo, event, [entity, data, event] as Object[])
    }

    void doBeforeRemove(RepositoryApi repo, GormEntity entity, Map args) {
        BeforeRemoveEvent event = new BeforeRemoveEvent(repo, entity, args)
        publishEvents(repo, event, [entity, event] as Object[])
    }

    void doAfterRemove(RepositoryApi repo, GormEntity entity, Map args) {
        AfterRemoveEvent event = new AfterRemoveEvent(repo, entity, args)
        publishEvents(repo, event, [entity, event] as Object[])

    }

//    private void findAndCacheListenerAnnotations(Class repoClass, Map<String, Method> listenerMethodMad) {
//        for(Method m in repoClass.getMethods()) {
//            ReflectionUtils.makeAccessible(m)
//            RepoListener sub = m.getAnnotation(RepoListener)
//            if(sub != null) {
//                Class[] parameterTypes = m.parameterTypes
//                boolean hasArgument = parameterTypes.length == 1
//                //2 params and first one is the Gorm Entity
//                if(parameterTypes.length == 2 && repoClass.isAssignableFrom(parameterTypes[0])) {
//                    listenerMethodMad[RepositoryEventType.BeforeBind.eventKey] = m
//                }
//            }
//        }
//    }
}
