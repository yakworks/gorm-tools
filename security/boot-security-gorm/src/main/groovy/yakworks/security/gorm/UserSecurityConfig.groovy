/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

import yakworks.security.user.CurrentUser


@CompileStatic
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix="app.security")
class UserSecurityConfig {

    //User specific timeouts
    Map<String, UserConfig> users

    Integer getQueryTimeout(CurrentUser currentUser) {
        if (!currentUser || !currentUser.loggedIn) null
        else {
            return getQueryTimeout(currentUser.user.username)
        }
    }

    Integer getQueryTimeout(String username) {
        Integer timeout = null
        if(users && users.containsKey(username)) {
            timeout = users[username].queryTimeout
        }
        return timeout
    }

    static class UserConfig {
        Integer queryMax
        Integer queryTimeout
    }

}
