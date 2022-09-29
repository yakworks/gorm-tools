/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.user

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import yakworks.security.SecService

/**
 * Contains statics to get the CurrentUserContext implementation
 */
@CompileStatic
class CurrentUser {

    private static SecService SECURITY_SERVICE

    static UserInfo getUserInfo(){
        SECURITY_SERVICE.getUserInfo()
    }

    @Autowired
    void setSecService(SecService secService){
        SECURITY_SERVICE = secService
    }
}
