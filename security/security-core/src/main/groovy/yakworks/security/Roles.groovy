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
    static final String ADMIN = "ADMIN" //super user
    static final String POWER_USER = "POWER_USER" //restricted super user.
    //default user, access to all the screens, not manager (cannot approve, view other's tasks or delete cash system data)
    static final String MANAGER = "MANAGER" //access to all user's tasks, approval, can delete cash system data
    static final String GUEST = "GUEST" //read only
    static final String CUSTOMER = "CUSTOMER" // single customer user

    //FIXME move into domain9
    static final String AR_MANAGER = "AR_MANAGER"  //can see other collector tasks, approvals

}
