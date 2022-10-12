/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.user


import groovy.transform.CompileStatic

import yakworks.security.user.UserInfo

/**
 * Simple method for getting user by id. Add to the impl of the userDetailService
 */
@CompileStatic
interface GetUserById {

    /**
     * Just return the simple UserInfo without processing.
     */
    UserInfo getById(Serializable id)

}
