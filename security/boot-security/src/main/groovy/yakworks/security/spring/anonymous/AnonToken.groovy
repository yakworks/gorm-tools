/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.anonymous

import groovy.transform.CompileStatic

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.SpringSecurityCoreVersion
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

/**
 * WIP for an anonymous token.
 */
@CompileStatic
class AnonToken extends AnonymousAuthenticationToken {
    // TODO use this
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID

    public static final String USERNAME = '__grails.anonymous.user__'
    public static final String PASSWORD = ''
    public static final String ROLE_NAME = 'ROLE_ANONYMOUS'
    public static final GrantedAuthority ROLE = new SimpleGrantedAuthority(ROLE_NAME)
    public static final List<GrantedAuthority> ROLES = Collections.singletonList(ROLE)
    public static final UserDetails USER_DETAILS = new User(USERNAME, PASSWORD, false, false, false, false, ROLES)

    /**
     * Constructor.
     */
    AnonToken(String key, Object details) {
        super(key, USER_DETAILS, ROLES)
        setDetails details
    }
}
