/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.config

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix="yakworks.gorm")
@CompileStatic
class GormConfig {

    /** almost never would this be false if including it unless turning off for a test */
    boolean enabled = true
    String hello
    /** whether to enable to hack that parses queryString if request.parameters is empty */
    boolean enableGrailsParams = true

    @Autowired
    AsyncConfig async

    @Autowired
    IdGeneratorConfig idGenerator

}
