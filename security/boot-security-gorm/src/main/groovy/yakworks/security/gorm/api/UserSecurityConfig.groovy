/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.api

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

import yakworks.gorm.config.QueryConfig
import yakworks.security.user.CurrentUser


@CompileStatic
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix="app.security")
class UserSecurityConfig {

    //User specific timeouts
    Map<String, UserConfig> users

    UserConfig getUserConfig(CurrentUser currentUser) {
        if (!currentUser || !currentUser.loggedIn){
            null
        } else {
            users[currentUser.user.username]
        }
    }

    Integer getQueryTimeout(CurrentUser currentUser) {
        if (!currentUser || !currentUser.loggedIn) null
        else {
            return getQueryTimeout(currentUser.user.username)
        }
    }

    Integer getMax(CurrentUser currentUser) {
        if (!currentUser || !currentUser.loggedIn) null
        else {
            return getMax(currentUser.user.username)
        }
    }

    Integer getQueryTimeout(String username) {
        Integer timeout = null
        if(users && users.containsKey(username)) {
            timeout = users[username].query.timeout
        }
        return timeout
    }

    Integer getMax(String username) {
        Integer max = null
        if(users && users.containsKey(username)) {
            max = users[username].query.max
        }
        return max
    }


    static class UserConfig {
        QueryConfig query = new QueryConfig()
    }

}
