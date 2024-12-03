/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.config.PropertySourcesConfig
import org.springframework.boot.ConfigurableBootstrapContext
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.ConfigurableEnvironment

/**
 * This plugin changes the defualts to what we think makes more sense. some of these must be done in
 * registered in META-INF/spring.factories
 *
 * They are what this woudl result in ...
 * grails {
 *   //gorm.flushMode = 'AUTO'
 *   gorm.failOnError = true
 *   gorm.default.mapping = {
 *     // id generator: 'yakworks.gorm.hibernate.SpringBeanIdGenerator'
 *     '*'(cascadeValidate: 'dirty')
 *   }
 *   gorm.default.constraints = {
 *     '*'(nullable: true)
 *   }
 * }
 *
 */
@SuppressWarnings(['ReturnsNullInsteadOfEmptyCollection'])
@CompileStatic
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) //let it run first so any config such as in ExternalConfigRunListener can override
class ConfigDefaultsRunListener implements SpringApplicationRunListener  {
    final SpringApplication application

    ConfigDefaultsRunListener(SpringApplication application, String[] args) {
        this.application = application
    }

    @Override
    void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {

        // Map currentProperties = getCurrentConfig(environment)
        // String encoding = environment.getProperty('grails.config.encoding', String, 'UTF-8')
        environment.propertySources.addFirst(ConfigDefaults.propertySource)
    }

    static Map getCurrentConfig(ConfigurableEnvironment environment) {
        return new PropertySourcesConfig(environment.propertySources)
    }

}
