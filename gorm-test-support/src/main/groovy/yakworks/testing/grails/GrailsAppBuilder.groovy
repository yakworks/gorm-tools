/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.grails

import groovy.transform.CompileStatic

import org.grails.testing.GrailsApplicationBuilder
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.env.ConfigurableEnvironment

/**
 * Copied in from grails-testing-support Created by jameskleeh on 5/31/17.
 * Adds the following,
 * - override to use AnnotationConfigApplicationContext
 * -
 */
@CompileStatic
class GrailsAppBuilder extends GrailsApplicationBuilder {

    @Override
    protected ConfigurableApplicationContext createMainContext(Object servletContext) {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext()

        // context.register(BasicConfiguration)

        ClassLoader applicationClassLoader = this.class.classLoader
        ConfigurableEnvironment configuredEnvironment = context.getEnvironment()

        ConfigurableBeanFactory beanFactory = context.getBeanFactory()
        prepareContext(context, beanFactory)

        context.register(TestConfiguration)
        context.scan("gorm.tools.config")
        context.refresh()
        context.registerShutdownHook()
        context
    }

}
