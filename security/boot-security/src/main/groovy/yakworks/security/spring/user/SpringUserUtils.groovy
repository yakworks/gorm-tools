/*
* Copyright 2006-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.user

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User

import yakworks.security.user.UserInfo

/**
 * Helper methods.
 *
 * @author Burt Beckwith
 */
@CompileStatic
@Slf4j
final class SpringUserUtils {

    private SpringUserUtils() { /* static only */ }

    @CompileDynamic //so we can set the username on User
    static void setUserUsername(User target, UserInfo sourceUser){
        target.@username = sourceUser.username
    }

    static void merge(UserInfo target, UserInfo sourceUser){
        ['id', 'name', 'displayName', 'email', 'orgId'].each{ String prop ->
            target[prop] = sourceUser[prop]
        }
    }

    /**
     * Converts spring authorites to Set of string names.
     */
    static Set<String> authoritiesToRoles(Collection<GrantedAuthority> authorities) {
        AuthorityUtils.authorityListToSet(authorities)
    }

    /**
     * Helper for constructor
     * If authorities=null then build from UserInfo, otherwise return whats passed in
     */
    static List<GrantedAuthority> rolesToAuthorities(Collection roleNames, Collection authorities = null) {
        if(authorities == null ){
            return AuthorityUtils.createAuthorityList(roleNames as String[])
        } else {
            return authorities as List<GrantedAuthority>
        }

    }
}
