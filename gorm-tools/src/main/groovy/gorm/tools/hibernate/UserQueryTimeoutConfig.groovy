/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@CompileStatic
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix="app.security")
class UserQueryTimeoutConfig {

    //User specific timeouts
    Map<String, UserTimeoutConfig> users

    static class UserTimeoutConfig {
        Integer queryTimeout
    }
}
