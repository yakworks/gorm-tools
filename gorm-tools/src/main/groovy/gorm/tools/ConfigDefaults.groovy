/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import groovy.transform.CompileStatic

import org.springframework.core.env.MapPropertySource

/**
 * The gorm defaults we think makes more sense to have out of the box. done here so it can be shared across the base test helpers
 * and for the ConfigDefaultsRunListener
 */
@SuppressWarnings(['ReturnsNullInsteadOfEmptyCollection'])
@CompileStatic
class ConfigDefaults {
    public static String springBeanIdGenMapping = 'id generator: "gorm.tools.hibernate.SpringBeanIdGenerator"'

    static String getConfigString(String idGenMapping){
        return """\
        grails {
            //gorm.flushMode = 'AUTO'
            gorm.failOnError = true
            gorm.default.mapping = {
                ${idGenMapping}
                '*'(cascadeValidate: 'dirty')
            }
            gorm.default.constraints = {
                '*'(nullable: true)
            }
        }
        """
    }

    // Load groovy config from resource
    static Map<String, Object> getConfigMap(boolean isProd = true) {
        ConfigSlurper slurper = new ConfigSlurper()
        String cfgStr = isProd ? getConfigString(springBeanIdGenMapping) : getConfigString('')
        // String cfgStr = getConfigString(springBeanIdGenMapping)
        ConfigObject configObject = slurper.parse(cfgStr)
        Map<String, Object> properties = configObject.flatten() as Map<String, Object>
        return properties
    }

    static MapPropertySource getPropertySource() {
        return new MapPropertySource("GormToolsConfigDefaults", getConfigMap(true))
    }

}
