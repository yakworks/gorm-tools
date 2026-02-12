/*
* Copyright 2006-2015 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.auditable

import groovy.util.logging.Slf4j

import org.grails.config.PropertySourcesConfig
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertySource

import grails.core.GrailsApplication
import grails.util.Holders

/**
 * Helper methods that use dynamic Groovy
 */
@Slf4j
class ReflectionUtils {

    // set at startup
    static GrailsApplication application

    private ReflectionUtils() {
        // static only
    }

    static Object getConfigProperty(String name, config = AuditLoggingConfigUtils.auditConfig) {
        def value = config
        name.split('\\.').each { String part -> value = value."$part" }
        value
    }

    static void setConfigProperty(String name, value) {
        def config = AuditLoggingConfigUtils.auditConfig

        List parts = name.split('\\.')
        name = parts.remove(parts.size() - 1)

        parts.each { String part -> config = config."$part" }

        config."$name" = value
    }

    static List asList(Object o) {
        o ? o as List : []
    }

    static ConfigObject getAuditConfig() {
        def grailsConfig = getApplication().config
        if (grailsConfig.auditLog) {
            log.error "Your auditLog configuration settings use the old prefix 'auditLog' but must now use 'grails.plugin.auditLog'"
        }
        grailsConfig.grails.plugin.auditLog
    }

    static void setAuditConfig(ConfigObject c) {
        ConfigObject config = new ConfigObject()
        config.grails.plugin.auditLog = c

        PropertySource propertySource = new MapPropertySource('AuditConfig', [:] << config)
        def propertySources = application.mainContext.environment.propertySources
        propertySources.addFirst propertySource

        getApplication().config = new PropertySourcesConfig(propertySources)
    }

    private static GrailsApplication getApplication() {
        if (!application) {
            application = Holders.grailsApplication
        }
        application
    }

}
