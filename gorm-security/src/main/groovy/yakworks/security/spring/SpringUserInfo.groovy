/*
* Copyright 2006-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

import yakworks.security.user.BasicUserInfo
import yakworks.security.user.UserInfo

import static yakworks.security.spring.SpringUserInfoUtils.*

/**
 * Grails security has a GrailsUser that it uses by default, this replaces it to remove confusion.
 * NOTES:
 *  - Extends the default Spring Security User class (which implements the UserDetails interface)
 *  - adds the id (the default implementation will set to the AppUser.id)
 *  - We dont use the AppUser gorm domain and instead create this with the data from AppUser instance
 *  - think of it as a DTO or serializable value object for a Spring Security User, this is whats stored in the context for the logged in user
 *
 * @see org.springframework.security.core.userdetails.User
 */
@SuppressWarnings(['ParameterCount'])
@InheritConstructors
@CompileStatic
class SpringUserInfo extends User implements SpringUserInfoTrait {
    private static final long serialVersionUID = 1

    @Override
    String getPasswordHash() {
        return this.password
    }

    @Override //UserInfo
    Set<String> getRoles() {
        SpringUserInfoUtils.authoritiesToRoles(this.authorities);
    }

    static SpringUserInfo of(UserInfo userInfo){
        def roles = (userInfo.roles ?: []) as List<String>
        return SpringUserInfo.of(userInfo, roles)
    }

    static SpringUserInfo of(UserInfo userInfo, Collection<String> roles){
        List<GrantedAuthority> authorities = rolesToAuthorities(roles)
        // password is required so make sure its filled even if its OAuth or ldap
        String passwordHash = userInfo.passwordHash ?: "N/A"
        def spu = new SpringUserInfo(userInfo.username, passwordHash, userInfo.enabled, true, true, true, authorities)
        merge(spu, userInfo)
        return spu
    }

    static SpringUserInfo create(Map props){
        def userInfo = BasicUserInfo.create(props)
        return SpringUserInfo.of(userInfo)
    }

    @CompileDynamic
    static void merge(UserInfo target, UserInfo sourceUser){
        ['id', 'name', 'displayName', 'email', 'orgId'].each{
            target[it] = sourceUser[it]
        }
    }
}
