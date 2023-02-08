/*
* Copyright 2013-2015 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.shiro

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class SpringSecurityRealm extends AuthorizingRealm {

    /** Dependency injection for the AuthenticationTrustResolver. */
    AuthenticationTrustResolver authenticationTrustResolver

    /** Dependency injection for the ShiroPermissionResolver. */
    ShiroPermissionResolver shiroPermissionResolver

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        if (principals == null) {
            throw new AuthorizationException('PrincipalCollection method argument cannot be null.')
        }

        Authentication authentication = SecurityContextHolder.context.authentication

        String username = (String)getAvailablePrincipal(principals)
        /*User user =*/
        getCurrentUser(username, authentication)

        Set<String> roleNames = authentication.authorities.collect { ((GrantedAuthority)it).authority } as Set

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames)
        info.stringPermissions = shiroPermissionResolver.resolvePermissions(username)

        if (log.debugEnabled) {
            log.debug 'AuthorizationInfo for user {}: {}', username, DefaultGroovyMethods.dump(info)
        }

        info
    }

    protected UserDetails getCurrentUser(String username, Authentication authentication) {
        if (!authentication || authenticationTrustResolver.isAnonymous(authentication)) {
            throw new AccountException('Not logged in or anonymous')
        }

        UserDetails user = (UserDetails)authentication.principal
        if (user.username != username) {
            throw new AccountException('Not logged in as expected user')
        }

        user
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken)token
        String username = upToken.username
        if (username == null) {
            throw new AccountException('Null usernames are not allowed by this realm.')
        }

        UserDetails user = getCurrentUser(username, SecurityContextHolder.context.authentication)
        new SimpleAuthenticationInfo(username, user.password.toCharArray(), getName())
    }
}
