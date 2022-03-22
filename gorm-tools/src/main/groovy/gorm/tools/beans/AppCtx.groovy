/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher

import grails.config.Config
import grails.core.GrailsApplication
import grails.util.Holders
import yakworks.i18n.icu.ICUMessageSource

/**
 * A static that uses the Holder to get the spring ApplicationContext it beans and the GrailsApplication
 * when in those cases where its not practical or possible to inject them (such as Traits for a persitenceEntity)
 * Obviously its highly recommended to not use this and use DI whenever possible.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@CompileStatic
class AppCtx {

    private static GrailsApplication cachedGrailsApplication
    //private static ApplicationContext cachedApplicationContext

    /**
     * @return the GrailsApplication
     */
    static GrailsApplication getGrails() {
        if (!cachedGrailsApplication) {
            cachedGrailsApplication = Holders.grailsApplication
        }
        return cachedGrailsApplication
    }

    /**
     * Used in tests to assign the right GrailsApplication
     */
    static void setGrailsApplication(GrailsApplication gapp) {
        cachedGrailsApplication = gapp
    }

    /**
     * @return the spring ApplicationContext
     */
    static ApplicationContext getCtx() {
//        if (!cachedApplicationContext) {
//            cachedApplicationContext = Holders.applicationContext
//        }
        return Holders.applicationContext
    }

    /**
     * @return the merged configs from application.yml, application.groovy, etc...
     */
    static Config getConfig() {
        Holders.config
    }

    /**
     * @return the merged configs from application.yml, application.groovy, etc...
     */
    static ICUMessageSource getMsgService() {
        get('messageSource', ICUMessageSource)
    }

    /**
     * call the ApplicationContext.getBean
     * @param name the bean name
     * @return the bean in the context
     */
    static Object get(String name){
        getCtx().getBean(name)
    }

    /**
     * Preferred method as typed checked, call the ApplicationContext.getBean
     *
     * @param name the name of the bean to retrieve
     * @param requiredType type the bean must match. Can be an interface or superclass
     * of the actual class, or {@code null} for any match. For example, if the value
     * is {@code Object.class}, this method will succeed whatever the class of the
     * returned instance.
     * @return the instance of the bean in the context
     */
    static <T> T get(String name, Class<T> requiredType){
        getCtx().getBean(name, requiredType)
    }

    static <T> T get(Class<T> requiredType){
        getCtx().getBean(requiredType)
    }

    static void publishEvent(ApplicationEvent event){
        //we use the grails.mainContext here because the appCtx scrambles during tests and gets lost
        ((ApplicationEventPublisher)getGrails().mainContext).publishEvent(event)
    }
}
