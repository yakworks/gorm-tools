/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.extensions

import groovy.transform.CompileStatic

import yakworks.rally.orgs.model.Org
import yakworks.security.user.CurrentUser

/**
 * Extensions to add getUserOrg to the CurrentUser
 */
@CompileStatic
class CurrentUserExt {

    static Long getOrgId(CurrentUser self) {
        self.user.orgId as Long
    }

    /**
     * gets the org from contact for the currently logged in user
     */
    static Org getOrg(CurrentUser self) {
        UserInfoExt.getOrg(self.user)
    }

    static boolean isCustomer(CurrentUser self) {
        UserInfoExt.isCustomer(self.user)
    }

}
