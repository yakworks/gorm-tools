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
@ConfigurationProperties(prefix="app.security")
class UserSecurityConfig {

    //User specific timeouts
    Map<String, UserConfig> users

    UserConfig getUserConfig(CurrentUser currentUser) {
        if (!users || !currentUser || !currentUser.loggedIn){
            null
        } else {
            users[currentUser.user.username]
        }
    }

    static class UserConfig {
        QueryConfig query = new QueryConfig()
    }

}
