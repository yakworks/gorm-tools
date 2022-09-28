/*
* Copyright 2006-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority

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

    /**
     * Extract the role names from authorities.
     * @param authorities the authorities (a collection or array of {@link GrantedAuthority}).
     * @return the names
     */
    static Set<String> authoritiesToRoles(Collection authorities) {
        AuthorityUtils.authorityListToSet(authorities)
        // def roles = authorities.collect { grantedAuthority ->
        //     ((GrantedAuthority)grantedAuthority).authority
        // }
        // return roles as Set<String>
    }

    static List<? extends GrantedAuthority> rolesToAuthorities(Set roleNames) {
        AuthorityUtils.createAuthorityList(roleNames as String[])
        // def authorities = roleNames.collect { role ->
        //     new SimpleGrantedAuthority(role as String)
        // }
        // return authorities
    }
}
