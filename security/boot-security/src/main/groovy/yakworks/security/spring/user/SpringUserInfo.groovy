/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.user

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

import yakworks.commons.model.Named
import yakworks.security.user.UserInfo

/**
 * Marries UserDetails and UserInfo
 */
@CompileStatic
trait SpringUserInfo implements UserDetails, Named, UserInfo {
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

    @CompileDynamic //traits are compiled dynamically anyway so take advantage
    static void copyUserInfo(UserInfo target, UserInfo sourceUser){
        ['id', 'name', 'displayName', 'email', 'orgId'].each{
            target[it] = sourceUser[it]
        }
    }

    @Override //UserInfo
    Set<String> getRoles() {
        SpringUserUtils.authoritiesToRoles((Collection<GrantedAuthority>)this.getAuthorities())
    }

    @Override
    String getPasswordHash() {
        return this.getPassword()
    }

}
