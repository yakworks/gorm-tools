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
import org.springframework.core.env.MapPropertySource

import grails.util.Environment

/**
 * This plugin changes the defualts to what we think makes more sense. some of these must be done in
 * registered in META-INF/spring.factories
 *
 * They are what this woudl result in ...
 * grails {
 *   //gorm.flushMode = 'AUTO'
 *   gorm.failOnError = true
 *   gorm.default.mapping = {
 *     // id generator: 'gorm.tools.hibernate.SpringBeanIdGenerator'
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

    // ResourceLoader defaultResourceLoader = new DefaultResourceLoader()
    // private YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader()
    // private PropertiesPropertySourceLoader propertiesPropertySourceLoader = new PropertiesPropertySourceLoader()
    //
    // private String userHome = System.properties.getProperty('user.home')
    // private String separator = System.properties.getProperty('file.separator')

    final SpringApplication application

    ConfigDefaultsRunListener(SpringApplication application, String[] args) {
        this.application = application
    }

    @Override
    void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {

        Map currentProperties = getCurrentConfig(environment)
        String encoding = environment.getProperty('grails.config.encoding', String, 'UTF-8')
        def gormConfig = loadGroovyConfig()
        environment.propertySources.addFirst(gormConfig)
    }

    // Load groovy config from resource
    private static MapPropertySource loadGroovyConfig() {
        ConfigSlurper slurper = new ConfigSlurper(Environment.current.name)
        ConfigObject configObject = slurper.parse("""\
            grails {
                //gorm.flushMode = 'AUTO'
                gorm.failOnError = true
                gorm.default.mapping = {
                    // id generator: 'gorm.tools.hibernate.SpringBeanIdGenerator'
                    '*'(cascadeValidate: 'dirty')
                }
                gorm.default.constraints = {
                    '*'(nullable: true)
                }
            }
        """)
        Map<String, Object> properties = configObject.flatten() as Map<String, Object>
        return new MapPropertySource("gormDefaults.groovy", properties)
    }

    static Map getCurrentConfig(ConfigurableEnvironment environment) {
        return new PropertySourcesConfig(environment.propertySources)
    }

}
