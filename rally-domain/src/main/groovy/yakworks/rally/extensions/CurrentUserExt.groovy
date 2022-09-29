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
import yakworks.security.user.CurrentUser
import yakworks.security.user.UserInfo

/**
 * Extensions to add getUserOrg to the CurrentUser
 */
@CompileStatic
class CurrentUserExt {

    /**
    * gets the org from contact for the currently logged in user
    */
    static Org getUserOrg(CurrentUser self){
        def uinfo = self.getUserInfo()
        Validate.notNull(uinfo, '[userInfo]')
        getUserOrg(self, uinfo)
    }

    /**
    * gets the org for the passed in UserInfo
    */
    @NullCheck
    static Org getUserOrg(CurrentUser self, UserInfo userInfo){
        Validate.notNull(userInfo.orgId, "userInfo.orgId is null for user:[id:${userInfo.id}, username: ${userInfo.username}]")
        return Org.repo.getWithTrx(userInfo.orgId)
    }

    static boolean isCustomer(CurrentUser self){
        def org = getUserOrg(self)
        return org.type == OrgType.Customer
    }

}
