/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.user

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

/**
 * Contains statics to get the CurrentUserContext implementation
 */
@CompileStatic
class CurrentUserHolder {

    private static CurrentUser CURRENT_USER

    static UserInfo getUserInfo(){
        CURRENT_USER.getUserInfo()
    }

    @Autowired
    void setCurrentUser(CurrentUser currentUser){
        CURRENT_USER = currentUser
    }
}
