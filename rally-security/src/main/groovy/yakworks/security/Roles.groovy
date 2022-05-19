/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import groovy.transform.CompileStatic

/**
 * Constants for role names, NOT restricted to these just for easier and cleaner usage.
 */
@CompileStatic
class Roles {
    // enabled by default in base data
    //full access, system user
    static final String ADMIN = "ADMIN" //alias
    static final String POWER_USER = "POWER_USER"
    //default user, access to all the screens, not manager (cannot approve, view other's tasks or delete cash system data)
    static final String MANAGER = "MANAGER" //access to all user's tasks, approval, can delete cash system data
    static final String GUEST = "GUEST" //read only
    static final String CUSTOMER = "CUSTOMER" //greenbill single customer user

    // disabled by default in base data, more speific roles
    static final String AR_COLLECTOR = "AR_COLLECTOR" //non cash
    static final String AR_MANAGER = "AR_MANAGER"  //can see other collector tasks, approvals

    static final String AUTOCASH = "AUTOCASH" //cash, cannot delete system data
    static final String AUTOCASH_MANAGER = "AUTOCASH_MANAGER" //cash, can delete system data
    static final String AUTOCASH_OFFSET = "AUTOCASH_OFFSET" //cash, can only do $0 payments

    static final String ADMIN_CONFIG = "ADMIN_CONFIG" //setup
    static final String ADMIN_SEC = "ADMIN_SEC" //user sec. management (acl)

    static final String SALES = "SALES" //review, approve disputes
    static final String BRANCH = "BRANCH"
    //branch, store user with limited access to customer and transaction screen only.

}
