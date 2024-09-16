/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.api

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import yakworks.gorm.api.support.DefaultQueryArgsValidator
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
        UserSecurityConfig.UserConfig userCfg = userSecurityConfig.getUserConfig(currentUser)
        Integer userTimeout = queryConfig.timeout
        if(userCfg?.query.timeout && userCfg.query.timeout > userTimeout) {
            userTimeout = userCfg.query.timeout
        }
        return userTimeout
    }

    @Override
    int getMax() {
        UserSecurityConfig.UserConfig userCfg = userSecurityConfig.getUserConfig(currentUser)
        Integer maxItems = queryConfig.max
        if(userCfg?.query.max && userCfg.query.max > maxItems) {
            maxItems = userCfg.query.max
        }
        return maxItems
    }
}
