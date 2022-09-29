/*
* Copyright 2006-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

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
final class SpringUserInfoUtils {

    /**
     * Used to ensure that all authenticated users have at least one granted authority to work
     * around Spring Security code that assumes at least one. By granting this non-authority,
     * the user can't do anything but gets past the somewhat arbitrary restrictions.
     */
    public static final String NO_ROLE = 'ROLE_NO_ROLES'

    private SpringUserInfoUtils() {
        // static only
    }

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

    static Set<String> authoritiesToRoles(Collection authorities) {
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
