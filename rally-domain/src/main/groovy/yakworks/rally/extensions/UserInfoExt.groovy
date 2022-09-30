/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.extensions

import groovy.transform.CompileStatic
import groovy.transform.NullCheck

import yakworks.commons.lang.Validate
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.security.user.UserInfo

/**
 * Extensions to add getOrg to UserInfo
 */
@CompileStatic
class UserInfoExt {

    /**
     * gets the org from orgId for this user
     */
    @NullCheck
    static Org getOrg(UserInfo userInfo){
        Validate.notNull(userInfo.orgId, "userInfo.orgId is null for user:[id:${userInfo.id}, username: ${userInfo.username}]")
        return Org.repo.getWithTrx(userInfo.orgId)
    }

    static boolean isCustomer(UserInfo self){
        def org = getOrg(self)
        return org.type == OrgType.Customer
    }

}
