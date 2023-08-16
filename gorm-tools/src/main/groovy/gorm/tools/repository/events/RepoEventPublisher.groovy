/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.ClassUtils
import org.springframework.util.ReflectionUtils
import org.springframework.validation.Errors

import gorm.tools.databinding.BindAction
import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import yakworks.spring.AppCtx

/**
 * Invokes "event methods" on Repository artifacts as well as publish spring events for @EventListeners
 * which can be Transactional and fires Grails events that will be asynchronous
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@Slf4j
@CompileStatic
class RepoEventPublisher {

    // @Autowired shoudl not need to set this in init but unit tests get scrambled so it done liek this for those
    // @Autowired
    // ApplicationEventPublisher applicationEventPublisher

    private final Map<String, Map<String, Method>> repoEventMethodCache = new ConcurrentHashMap<>()

    @Autowired(required = false)
    List<GormRepo> repoBeans

    @PostConstruct
    void init() {
        scanAndCacheEventsMethods()
        // applicationEventPublisher = (ApplicationEventPublisher) grailsApplication.mainContext
        log.debug("scanned and found events in repoEventMethodCache, size: ${repoEventMethodCache.size()}")
    }

    //iterates over Repos and cache events
    void scanAndCacheEventsMethods() {
        if(!repoBeans) return
        //def repoBeanClasses = RepoUtil.getRepoClasses()
        //DefaultGormRepo are set up automatically for an Entity and wont have event methods.
        List<Class<?>> repoBeanClasses = repoBeans.collect{ getRepoClass(it.class) }.findAll{ it != DefaultGormRepo }

        for (Class repoClass : repoBeanClasses) {
            cacheEventsMethods(repoClass)
        }
    }

    void cacheEventsMethods(Class repoClass) {
        Map<String, Method> eventMethodMap = new ConcurrentHashMap<>()
        repoEventMethodCache.put(repoClass.simpleName, eventMethodMap)

        RepositoryEventType.values().each { RepositoryEventType et ->
            findAndCacheEventMethods(et.eventKey, repoClass, eventMethodMap)
        }
    }

    private void findAndCacheEventMethods(String eventKey, Class repoClass, Map<String, Method> events) {
        Method method = ReflectionUtils.findMethod(repoClass, eventKey, null)
        RepoListener ann = method?.getAnnotation(RepoListener)
        if (method != null && ann)
            events[eventKey] = method
    }

    public <D> void publishEvents(GormRepo<D> repo, RepositoryEvent<D> event, Object... methodArgs) {
        //invokeListenerMethod(repo, event)
        invokeEventMethod(repo, event.eventKey, methodArgs)
        if (!repo.enableEvents) return
        AppCtx.publishEvent(event)
        //eventBus.notify(event.routingKey, event)
    }

//    void invokeListenerMethod(Object repo, RepositoryEvent event) {
//        Map<String, Method> listenerMap = repoListenerMethodCache.get(repo.class.simpleName)
//        Method method = listenerMap?.get(event.eventKey)
//        if (!method) return
//        ReflectionUtils.invokeMethod(method, repo, event)
//    }

    void invokeEventMethod(GormRepo repo, String eventKey, Object... methodArgs) {
        Map<String, Method> eventMethodMap = repoEventMethodCache.get(getRepoClass(repo.class).simpleName)
        //if (!eventMethodMap) return //eventMethodMap = cacheEventsMethods(repo.class)
        Method method = eventMethodMap?.get(eventKey)
        if (!method) return
        //tuncate the args to the number of params for the method
        Object[] truncMethArgs = methodArgs[0..method.parameterCount-1]
        ReflectionUtils.invokeMethod(method, repo, truncMethArgs)
    }

    public <D> void doBeforeValidate(GormRepo<D> repo, D entity, Map args) {
        def event = new BeforeValidateEvent<D>(repo, entity, PersistArgs.of(args))
        publishEvents(repo, event, [entity] as Object[])
    }

    public <D> void doBeforeValidate(GormRepo<D> repo, D entity, Errors errors, Map args) {
        def event = new BeforeValidateEvent<D>(repo, entity, PersistArgs.of(args))
        publishEvents(repo, event, [entity, errors] as Object[])
    }

    public <D> void doBeforePersist(GormRepo<D> repo, D entity, PersistArgs args) {
        def event = new BeforePersistEvent<D>(repo, entity, args)
        publishEvents(repo, event, [entity, event] as Object[])
    }

    public <D> void doAfterPersist(GormRepo<D> repo, D entity, PersistArgs args) {
        def event = new AfterPersistEvent<D>(repo, entity, args)
        publishEvents(repo, event, [entity, event] as Object[])
    }

    public <D> void doBeforeBind(GormRepo<D> repo, D entity, Map data, BindAction bindAction, PersistArgs args) {
        def event = new BeforeBindEvent<D>(repo, entity, data, bindAction, args)
        publishEvents(repo, event, [entity, data, event] as Object[])
    }

    public <D> void doAfterBind(GormRepo<D> repo, D entity, Map data, BindAction bindAction, PersistArgs args) {
        def event = new AfterBindEvent<D>(repo, entity, data, bindAction, args)
        publishEvents(repo, event, [entity, data, event] as Object[])
    }

    public <D> void doBeforeRemove(GormRepo<D> repo, D entity, PersistArgs args) {
        def event = new BeforeRemoveEvent<D>(repo, entity, args)
        publishEvents(repo, event, [entity, event] as Object[])
    }

    public <D> void doAfterRemove(GormRepo<D> repo, D entity, PersistArgs args) {
        def event = new AfterRemoveEvent<D>(repo, entity, args)
        publishEvents(repo, event, [entity, event] as Object[])

    }

    /**
     * Unwraps the proxy and gives the original repo class
     * If repo class is a spring proxy (eg because of @Cacheable etc) it would not be able to find event methods
     * See `findAndCacheEventMethods` & `invokeEventMethod` which would otherwise fail to find event methods
     */
    Class getRepoClass(Class clazz) {
        return ClassUtils.getUserClass(clazz)
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
