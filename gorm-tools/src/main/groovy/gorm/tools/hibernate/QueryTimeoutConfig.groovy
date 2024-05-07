/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix="yakworks.gorm.timeouts")
class QueryTimeoutConfig {

    //Query timeout
    Integer query

    //transaction timeout
    @Value('${spring.transaction.default-timeout:-1}')
    Integer transaction

    //User specific timeouts
    //@Josh cant inject it from app.security - @Value annotation fails to convert property type. But ConfigurationProperties works
    Map<String, UserTimeoutConfig> users

    static class UserTimeoutConfig {
      Integer queryTimeout
    }
}
