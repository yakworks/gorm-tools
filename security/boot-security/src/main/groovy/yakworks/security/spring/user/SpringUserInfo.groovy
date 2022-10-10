/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.user


import groovy.transform.CompileStatic

import org.springframework.security.core.GrantedAuthority
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
    /** UserInfo &  UserDetails*/
    final String username
    /** UserInfo , getPssword returns this*/
    final String passwordHash

    /** UserInfo */
    String name
    // /** UserInfo &  UserDetails*/
    // String username
    /** UserInfo */
    String displayName
    /** UserInfo */
    String email
    /** UserInfo */
    Serializable orgId
    /** roles */
    Set<String> roles = [] as Set<String>
    /** future use */
    Map<String, Object> userProfile = [:] as Map<String, Object>

    //implements the extra UserDetails
    boolean accountNonExpired = true
    boolean accountNonLocked = true
    boolean credentialsNonExpired = true
    boolean enabled = true

    /** the orginal details (usually ip addy and sessionId) from the Authentication */
    Object auditDetails

    // @Override //UserInfo
    // Set<String> getRoles() {
    //     SpringUserUtils.authoritiesToRoles((Collection<GrantedAuthority>)this.getAuthorities())
    // }

    @Override
    String getPassword() {
        return getPasswordHash()
    }

    @Override
    Collection<? extends GrantedAuthority> getAuthorities(){
        return SpringUserUtils.rolesToAuthorities(roles)
    }

    /** merges only a subset of the data */
    def merge(UserInfo sourceUser){
        ['id', 'name', 'displayName', 'email', 'orgId', 'roles'].each{ String prop ->
            this[prop] = sourceUser[prop]
        }
        return this
    }

}
