/*
* Copyright 2006-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.user

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor

import yakworks.security.user.BasicUserInfo
import yakworks.security.user.UserInfo

/**
 * Grails security has a GrailsUser that it uses by default, this replaces it to remove confusion and make it Spring thats all the we depend on.
 * NOTES:
 *  - Extends the default Spring Security User class (which implements the UserDetails interface)
 *  - adds the id (the default implementation will set to the AppUser.id)
 *  - We dont use the AppUser gorm domain and instead create this with the data from AppUser instance
 *  - think of it as a DTO or serializable value object for a Spring Security User, this is whats stored in the context for the logged in user
 *
 * @see org.springframework.security.core.userdetails.User
 */
@SuppressWarnings(['ParameterCount'])
@MapConstructor
@CompileStatic
class SpringUser implements SpringUserInfo {
    private static final long serialVersionUID = 1
    /** UserInfo &  UserDetails*/
    final String username
    final String passwordHash

    //implements the extra UserDetails
    boolean accountNonExpired = true
    boolean accountNonLocked = true
    boolean credentialsNonExpired = true
    boolean enabled = true


    static SpringUser of(UserInfo userInfo){
        Set<String> roles = (userInfo.roles ?: []) as Set<String>
        Set<String> permissions = (userInfo.permissions ?: []) as Set<String>
        return SpringUser.of(userInfo, roles, permissions)
    }

    static SpringUser of(UserInfo userInfo, Collection<String> roles, Collection<String> permissions = []){
        // password is required so make sure its filled even if its OAuth or ldap
        String passwordHash = userInfo.passwordHash ?: "N/A"
        def spu = new SpringUser(
            username: userInfo.username,
            passwordHash: passwordHash,
            enabled: userInfo.enabled
        )
        spu.roles = roles as Set<String>
        spu.permissions = permissions as Set<String>
        spu.merge(userInfo)
        return spu
    }

    //Used mostly for testing
    static SpringUser of(String username, Collection<String> roles){
        return SpringUser.of(BasicUserInfo.of(username), roles)
    }

    static SpringUser create(Map props){
        def userInfo = BasicUserInfo.create(props)
        return SpringUser.of(userInfo)
    }

}
