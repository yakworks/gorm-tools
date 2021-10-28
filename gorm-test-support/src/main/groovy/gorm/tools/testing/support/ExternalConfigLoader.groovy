/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.support

import java.lang.reflect.Constructor

import groovy.transform.CompileDynamic

import org.springframework.beans.BeansException
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

import grails.boot.config.GrailsApplicationPostProcessor

/**
 * Runs ExternalConfigRunListener if external-config plugin is installed,
 * to make the configuration available during unit tests
 */
@CompileDynamic
@Order(Ordered.HIGHEST_PRECEDENCE)
class ExternalConfigLoader implements ApplicationContextAware{
    /**
     * Calls ExternalConfigRunListener from the plugin if installed which adds the property sources for external config
     * and then grailsApplicationPostProcessor.loadApplicationConfig
     */
    @Override
    @SuppressWarnings(['ClassForName', 'EmptyCatchBlock'])
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        GrailsApplicationPostProcessor postProcessor = applicationContext.getBean('grailsApplicationPostProcessor')

        try {
            //run ExternalConfigRunListener it will add all the property sources to environment
            Constructor c = Class.forName("grails.plugin.externalconfig.ExternalConfigRunListener").getConstructor(SpringApplication, String[].class)
            Object listener = c.newInstance(null, null)
            listener.environmentPrepared(applicationContext.environment)
            postProcessor.loadApplicationConfig()
        }catch (ClassNotFoundException e) {
            //external config plugin is not installed
        }
    }
}
