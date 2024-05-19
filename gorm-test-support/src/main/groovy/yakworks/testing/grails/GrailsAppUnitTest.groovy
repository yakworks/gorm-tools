/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.grails

import groovy.transform.CompileStatic

import org.grails.core.lifecycle.ShutdownOperations
import org.grails.testing.GrailsUnitTest
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.util.ClassUtils

import grails.core.DefaultGrailsApplication
import grails.core.GrailsApplication
import grails.spring.BeanBuilder
import grails.util.Holders
import grails.validation.DeferredBindingActions
import yakworks.commons.beans.PropertyTools
import yakworks.commons.lang.NameUtils
import yakworks.gorm.boot.SpringBeanUtils

/**
 * replacement of the GrailUnitTest and adds the following
 * - in order to build a GrailsApplication with a Spring Boot AnnotationConfigApplicationContext
 * - runs autowire for any annotations such as @Autowired, also runs any beanPostProcessing so @PostConstruct methods should get called too
 * - fixes defineBeans for
 */
@SuppressWarnings(['AssignmentToStaticFieldFromInstanceMethod', ''])
@CompileStatic
trait GrailsAppUnitTest extends GrailsUnitTest{

    private static GrailsApplication _grailsApp
    boolean initialized
    private static Object _servletContext

    //hack, see explanation on isDataRepoTest in GrailsAppBuilder. has to do with context.'annotation-config'() being needed
    boolean getDataRepoTest(){false}

    /**
     * @return the servlet context
     */
    @Override
    Object getOptionalServletContext() {
        _servletContext
    }

    @Override
    ConfigurableApplicationContext getApplicationContext() {
        (ConfigurableApplicationContext) getGrailsApplication().mainContext
    }

    /** convenience shortcut for applicationContext */
    ConfigurableApplicationContext getCtx() {
        getApplicationContext()
    }

    @Override
    GrailsApplication getGrailsApplication() {
        if (_grailsApp == null) {
            def builder = new GrailsAppBuilder(
                    doWithSpring: doWithSpring(),
                    doWithConfig: doWithConfig(),
                    includePlugins: getIncludePlugins(),
                    loadExternalBeans: loadExternalBeans(),
                    localOverride: localOverride,
                    isDataRepoTest: getDataRepoTest()
            ).build()
            _grailsApp = builder.grailsApplication
            _servletContext = builder.servletContext
            registerSpringBeansMap()
            initialized = true
        }

        _grailsApp
    }

    /**
     * if springBeans static prop or getter is defined then this will do the SpringBeanUtils.registerBeans
     */
    void registerSpringBeansMap(){
        Map springBeanMap = getSpringBeanMap()
        if(springBeanMap){
            SpringBeanUtils.registerBeans((BeanDefinitionRegistry)applicationContext, springBeanMap as Map<String, Object>)
        }
    }

    Map getSpringBeanMap(){
        def springBeans = PropertyTools.getOrNull(this, 'springBeans')
        Map springBeanMap = [:] as Map<String, Object>
        if(springBeans) {
            if (springBeans instanceof List) {
                //assumes its a list of Classes or Maps. If Class then just use the simple property name for key.
                //if its a Map then should be normal and will be merged in.
                for (Object bentry : springBeans) {
                    if (bentry instanceof Class) {
                        String beanName = NameUtils.getPropertyName(bentry)
                        springBeanMap[beanName] = bentry
                    } else if (bentry instanceof Map) {
                        springBeanMap.putAll(bentry)
                    } else {
                        throw new IllegalArgumentException("Items in springBeans List property must either be a Class or a Map")
                    }
                }
            } else {
                springBeanMap = springBeans as Map
            }
        }
        return springBeanMap
    }

    /**
     * Override to remove the call to preInstantiateSingletons which fails in certain circumstances with AnnotationConfigApplicationContext
     */
    @Override
    void defineBeans(Closure closure) {
        def binding = new Binding()
        def bb = new BeanBuilder(null, null, grailsApplication.getClassLoader())
        binding.setVariable "application", grailsApplication
        bb.setBinding binding
        bb.beans(closure)
        bb.registerBeans((BeanDefinitionRegistry)applicationContext)
        //not working with AnnotationConfigApplicationContext when its called multiple times
        // applicationContext.beanFactory.preInstantiateSingletons()
    }

    void registerBeans(Map<String, Object> beanMap) {
        SpringBeanUtils.registerBeans((BeanDefinitionRegistry)applicationContext, beanMap)
    }

    /**
     * since we need to store a local ref to _grailsApp this cleans up that.
     */
    @Override
    void cleanupGrailsApplication() {
        if (_grailsApp != null) {
            if (_grailsApp instanceof DefaultGrailsApplication) {
                ((DefaultGrailsApplication)_grailsApp).clear()
            }

            ApplicationContext applicationContext = grailsApplication.getParentContext()

            if (applicationContext instanceof ConfigurableApplicationContext) {
                if (((ConfigurableApplicationContext) applicationContext).isActive()) {
                    if(grailsApplication.mainContext instanceof Closeable) {
                        ((Closeable)grailsApplication.mainContext).close()
                    }
                    if (applicationContext instanceof Closeable) {
                        ((Closeable)applicationContext).close()
                    }
                }
            }

            ShutdownOperations.runOperations()
            DeferredBindingActions.clear()

            this._grailsApp = null
            cleanupPromiseFactory()
            Holders.clear()
        }
        initialized = false
    }

    private void cleanupPromiseFactory() {
        ClassLoader classLoader = getClass().classLoader
        if (ClassUtils.isPresent("grails.async.Promises", classLoader)) {
            grails.async.Promises.promiseFactory = null
        }
    }
}
