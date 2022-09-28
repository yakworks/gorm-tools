/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import yakworks.commons.lang.Validate
import yakworks.rally.orgs.model.Org
import yakworks.security.SecService
import yakworks.security.UserInfo

@Service @Lazy
@Slf4j
@CompileStatic
class UserOrgService {

    @Autowired(required=false) SecService secService

    /**
     * gets the org from contact for the currently logged in user
     */
    Org getUserOrg(){
        getUserOrg(secService.getUser())
    }

    /**
     * gets the org for the passed in AppUser
     */
    Org getUserOrg(UserInfo userInfo){
        assert userInfo
        Validate.notNull(userInfo.orgId, "User.orgId is null for user ${userInfo.id}:${userInfo.username}")
        return Org.get(userInfo.orgId)
    }
}
