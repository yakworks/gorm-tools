/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.api

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import yakworks.gorm.api.support.DefaultQueryArgsValidator
import yakworks.gorm.config.QueryConfig
import yakworks.security.user.CurrentUser

/**
 * Sets user specific extended value for query timeout and max, if configured for logged in user.
 */
@CompileStatic
class UserQueryArgsValidator extends DefaultQueryArgsValidator {

    @Autowired UserSecurityConfig userSecurityConfig
    @Autowired CurrentUser currentUser

    @Override
    int getTimeout() {
        QueryConfig userQueryCfg = getUserQueryConfig()
        Integer userTimeout = queryConfig.timeout
        if(userQueryCfg?.timeout && userQueryCfg.timeout > userTimeout) {
            userTimeout = userQueryCfg.timeout
        }
        return userTimeout
    }

    @Override
    int getMax() {
        QueryConfig userQueryCfg = getUserQueryConfig()
        Integer maxItems = queryConfig.max
        if(userQueryCfg?.max && (userQueryCfg.max > maxItems)) {
            maxItems = userQueryCfg.max
        }
        return maxItems
    }

    QueryConfig getUserQueryConfig(){
        UserSecurityConfig.UserConfig userCfg = userSecurityConfig.getUserConfig(currentUser)
        return userCfg?.query
    }
}
