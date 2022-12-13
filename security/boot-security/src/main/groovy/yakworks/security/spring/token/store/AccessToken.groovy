/*
* Copyright 2013-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token.store

import groovy.transform.CompileStatic
import groovy.transform.ToString

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

import com.nimbusds.jwt.JWT

/**
 * TEMPORARY copy from the grails rest plugin.
 */
@ToString(includeNames = true, includeSuper = true,
    includes = ['principal', 'accessToken', 'accessTokenJwt', 'refreshToken', 'refreshTokenJwt', 'expiration'])
@CompileStatic
class AccessToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1L

    String accessToken
    JWT accessTokenJwt

    Integer expiration

    String refreshToken
    JWT refreshTokenJwt

    /** The username */
    UserDetails principal

    AccessToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities)
        super.setAuthenticated(true)
    }

    AccessToken(UserDetails principal, Collection<? extends GrantedAuthority> authorities, String accessToken, String refreshToken = null,
                Integer expiration = null, JWT accessTokenJwt = null, JWT refreshTokenJwt = null) {
        this(authorities)
        this.principal = principal
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.expiration = expiration
        this.accessTokenJwt = accessTokenJwt
        this.refreshTokenJwt = refreshTokenJwt
    }

    AccessToken(String accessToken, String refreshToken = null, Integer expiration = null) {
        super(null)
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.expiration = expiration
        super.setAuthenticated(false)
    }

    Object getCredentials() {
        return null
    }

    void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead")
        }

        super.setAuthenticated(false)
    }

}
