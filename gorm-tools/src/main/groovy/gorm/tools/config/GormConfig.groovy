/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.config


import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

import yakworks.spring.SpringEnvironment

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix="gorm.tools")
@CompileStatic
class GormConfig implements SpringEnvironment{

    /** almost never would this be false if including it unless turning off for a test */
    boolean enabled = true
    String hello

    @Autowired
    AsyncConfig async

    @Autowired
    IdGeneratorConfig idGenerator

}
