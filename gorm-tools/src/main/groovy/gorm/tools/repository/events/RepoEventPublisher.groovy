/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.util.ReflectionUtils
import org.springframework.validation.Errors

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import grails.core.GrailsApplication

/**
 * Invokes "event methods" on Repository artifacts as well as publish spring events for @EventListeners
 * which can be Transactional and fires Grails events that will be asynchronous
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class RepoEventPublisher {

    @Autowired
    private GrailsApplication grailsApplication

    // @Autowired
    // private EventBus eventBus

    // @Autowired
    private ApplicationEventPublisher applicationEventPublisher

    private final Map<String, Map<String, Method>> repoEventMethodCache = new ConcurrentHashMap<>()
    //private final Map<String, Map<String, Method>> repoListenerMethodCache = new ConcurrentHashMap<>()

    @PostConstruct
    void init() {
        for (Class repoClass : grailsApplication.getArtefacts(RepositoryArtefactHandler.TYPE)*.clazz) {
            cacheEventsMethods(repoClass)
        }
        def mainContext = grailsApplication.mainContext
        applicationEventPublisher = (ApplicationEventPublisher) grailsApplication.mainContext
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

    public <D> void publishEvents(GormRepo<D> repo, RepositoryEvent<D> event, Object... methodArgs) {
        //invokeListenerMethod(repo, event)
        invokeEventMethod(repo, event.eventKey, methodArgs)
        if (!repo.enableEvents) return
        applicationEventPublisher.publishEvent(event)
        //eventBus.notify(event.routingKey, event)
    }

//    void invokeListenerMethod(Object repo, RepositoryEvent event) {
//        Map<String, Method> listenerMap = repoListenerMethodCache.get(repo.class.simpleName)
//        Method method = listenerMap?.get(event.eventKey)
//        if (!method) return
//        ReflectionUtils.invokeMethod(method, repo, event)
//    }

    void invokeEventMethod(GormRepo repo, String eventKey, Object... methodArgs) {
        Map<String, Method> eventMethodMap = repoEventMethodCache.get(repo.class.simpleName)
        //if (!eventMethodMap) return //eventMethodMap = cacheEventsMethods(repo.class)
        Method method = eventMethodMap?.get(eventKey)
        if (!method) return
        //tuncate the args to the number of params for the method
        Object[] truncMethArgs = methodArgs[0..method.parameterCount-1]
        ReflectionUtils.invokeMethod(method, repo, truncMethArgs)
    }

    public <D> void doBeforeValidate(GormRepo<D> repo, D entity, Map args) {
        def event = new BeforeValidateEvent<D>(repo, entity, args)
        publishEvents(repo, event, [entity] as Object[])
    }

    public <D> void doBeforeValidate(GormRepo<D> repo, D entity, Errors errors, Map args) {
        def event = new BeforeValidateEvent<D>(repo, entity, args)
        publishEvents(repo, event, [entity, errors] as Object[])
    }

    public <D> void doBeforePersist(GormRepo<D> repo, D entity, Map args) {
        def event = new BeforePersistEvent<D>(repo, entity, args)
        publishEvents(repo, event, [entity, event] as Object[])
    }

    public <D> void doAfterPersist(GormRepo<D> repo, D entity, Map args) {
        def event = new AfterPersistEvent<D>(repo, entity, args)
        publishEvents(repo, event, [entity, event] as Object[])
    }

    public <D> void doBeforeBind(GormRepo<D> repo, D entity, Map data, BindAction bindAction, Map args) {
        def event = new BeforeBindEvent<D>(repo, entity, data, bindAction, args)
        publishEvents(repo, event, [entity, data, event] as Object[])
    }

    public <D> void doAfterBind(GormRepo<D> repo, D entity, Map data, BindAction bindAction, Map args) {
        def event = new AfterBindEvent<D>(repo, entity, data, bindAction, args)
        publishEvents(repo, event, [entity, data, event] as Object[])
    }

    public <D> void doBeforeRemove(GormRepo<D> repo, D entity, Map args) {
        def event = new BeforeRemoveEvent<D>(repo, entity, args)
        publishEvents(repo, event, [entity, event] as Object[])
    }

    public <D> void doAfterRemove(GormRepo<D> repo, D entity, Map args) {
        def event = new AfterRemoveEvent<D>(repo, entity, args)
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
