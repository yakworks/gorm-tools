/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.grails

import groovy.transform.CompileStatic

import org.grails.testing.GrailsApplicationBuilder
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.convert.ConversionService
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.env.PropertyResolver

import grails.boot.GrailsApp
import io.micronaut.context.ApplicationContextConfiguration
import io.micronaut.context.DefaultApplicationContext
import io.micronaut.spring.context.factory.MicronautBeanFactoryConfiguration

/**
 * Copied in from grails-testing-support Created by jameskleeh on 5/31/17.
 * override to use AnnotationConfigApplicationContext
 */
@CompileStatic
class GrailsAppBuilder extends GrailsApplicationBuilder {

    @Override
    protected ConfigurableApplicationContext createMainContext(Object servletContext) {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext()

        if (isServletApiPresent && servletContext != null) {
            context = new AnnotationConfigApplicationContext() //AnnotationConfigServletWebServerApplicationContext()
        } else {
            context = new AnnotationConfigApplicationContext()
        }
        context.register(BasicConfiguration)
        ClassLoader applicationClassLoader = this.class.classLoader
        ConfigurableEnvironment configuredEnvironment = context.getEnvironment()
        ApplicationContextConfiguration micronautConfiguration = new ApplicationContextConfiguration() {
            @Override
            List<String> getEnvironments() {
                if (configuredEnvironment != null) {
                    return configuredEnvironment.getActiveProfiles().toList()
                } else {
                    return Collections.emptyList()
                }
            }

            @Override
            Optional<Boolean> getDeduceEnvironments() {
                return Optional.of(false)
            }

            @Override
            ClassLoader getClassLoader() {
                return applicationClassLoader
            }
        }

        ConfigurableBeanFactory beanFactory = context.getBeanFactory()
        List beanExcludes = []
        beanExcludes.add(ConversionService)
        beanExcludes.add(Environment)
        beanExcludes.add(PropertyResolver)
        beanExcludes.add(ConfigurableEnvironment)
        def objectMapper = io.micronaut.core.reflect.ClassUtils.forName("com.fasterxml.jackson.databind.ObjectMapper", context.getClassLoader()).orElse(null)
        if (objectMapper != null) {
            beanExcludes.add(objectMapper)
        }
        def micronautContext = new DefaultApplicationContext(micronautConfiguration);
        micronautContext
                .environment
                .addPropertySource("grails-config", [(MicronautBeanFactoryConfiguration.PREFIX + ".bean-excludes"): (Object) beanExcludes])
        micronautContext.start()
        ConfigurableApplicationContext parentContext = micronautContext.getBean(ConfigurableApplicationContext)
        ((DefaultListableBeanFactory) beanFactory)
                .setAllowBeanDefinitionOverriding(true);
        ((DefaultListableBeanFactory) beanFactory)
                .setAllowCircularReferences(true);
        context.setParent(
                parentContext
        )
        context.addApplicationListener(new GrailsApp.MicronautShutdownListener(micronautContext))
        prepareContext(context, beanFactory)
        context.refresh()
        context.registerShutdownHook()

        context
    }

}
