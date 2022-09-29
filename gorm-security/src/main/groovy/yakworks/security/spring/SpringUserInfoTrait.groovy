/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.security.core.userdetails.User

import yakworks.commons.model.Named
import yakworks.security.user.UserInfo

/**
 * Default implementation of UserInfo, used in testing and as a DTO to setup a user.
 */
@CompileStatic
trait SpringUserInfoTrait implements Named, UserInfo {
    /** UserInfo */
    Serializable id
    /** UserInfo */
    String displayName
    /** UserInfo */
    String email
    /** UserInfo */
    Serializable orgId

    Map<String, Object> userProfile

    @CompileDynamic
    static void copyUserInfo(User target, UserInfo sourceUser){
        target.@username = sourceUser.username
    }

    @CompileDynamic
    static void copyUserInfo(UserInfo target, UserInfo sourceUser){
        ['id', 'name', 'displayName', 'email', 'orgId'].each{
            target[it] = sourceUser[it]
        }
    }

}
