/*
* Copyright 2006-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.user


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.util.Assert

import yakworks.security.user.BasicUserInfo
import yakworks.security.user.UserInfo

/**
 * Helper methods.
 *
 * @author Burt Beckwith
 */
@CompileStatic
@Slf4j
final class SpringUserUtils {

    public static Long SYSTEM_ORGID = 2

    private SpringUserUtils() { /* static only */ }


    static void merge(UserInfo target, UserInfo sourceUser){
        ['id', 'name', 'displayName', 'email', 'orgId'].each{ String prop ->
            target[prop] = sourceUser[prop]
        }
    }

    /**
     * Converts spring authorites to Set of string names.
     */
    static Set<String> authoritiesToRoles(Collection<GrantedAuthority> authorities) {
        // AuthorityUtils.authorityListToSet(authorities)
        authorityListToRoleSet(authorities)
    }

    static Set<String> authorityListToRoleSet(Collection<? extends GrantedAuthority> userAuthorities) {
        Assert.notNull(userAuthorities, "userAuthorities cannot be null");
        Set<String> set = new HashSet<>(userAuthorities.size());
        for (GrantedAuthority authority : userAuthorities) {
            String authName = authority.getAuthority().substring('ROLE_'.length())
            String roleName = authName.startsWith('ROLE_') ? authName - 'ROLE_' : authName
            set.add(roleName);
        }
        return set;
    }

    /**
     * Helper for constructor
     * If authorities=null then build from UserInfo, otherwise return whats passed in
     */
    static List<GrantedAuthority> rolesToAuthorities(Collection roleNames, Collection authorities = null) {
        if(authorities == null ){
            return createAuthorityList(roleNames as String[])
        } else {
            return authorities as List<GrantedAuthority>
        }
    }

    static List<GrantedAuthority> createAuthorityList(String... authorities) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(authorities.length);
        for (String authority : authorities) {
            if(!authority.startsWith('ROLE_')) authority = "ROLE_$authority".toString()
            grantedAuthorities.add( new SimpleGrantedAuthority(authority) );
        }
        return grantedAuthorities;
    }


        static List<GrantedAuthority> permissionsToAuthorities(Collection<String> permissions) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(permissions.size());
        for (String authority : permissions) {
            grantedAuthorities.add( new SimpleGrantedAuthority(authority) );
        }
        return grantedAuthorities;
    }

    static SpringUserInfo buildSpringUser(String username, String pwd, List roles, Long id, Long orgId){
        def u = new BasicUserInfo(username: username, passwordHash: pwd, roles: roles as Set, id: id, orgId: orgId)
        SpringUser.of(u)
    }

    static SpringUserInfo systemUser(){
        def u = new BasicUserInfo(username: 'system', passwordHash: "N/A", roles: ['ADMIN'] as Set, id: 1L, orgId: SYSTEM_ORGID)
        SpringUser.of(u)
    }
}
