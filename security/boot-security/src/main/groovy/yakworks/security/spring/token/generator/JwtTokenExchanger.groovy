/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token.generator

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Service to get a token for a different user.
 * Assumes that admin is already logged in and just manages generating a new token
 */
@CompileStatic
class JwtTokenExchanger {

    @Autowired JwtTokenGenerator jwtTokenGenerator
    //@Autowired JwtProperties jwtProperties
    @Autowired UserDetailsService userDetailsService

    Jwt exchange(Authentication authentication) {
        return jwtTokenGenerator.generate(authentication)
    }

    Jwt exchange(String username) {
        def auth = getAuthenticationToken(username)
        def jwt = jwtTokenGenerator.generate(auth)
        return jwt
    }

    /**
     * Looks up username and creates a UsernamePasswordAuthenticationToken that can be used to pass
     * NOTE does not authenticate
     * @param username the username to lookup
     * @return the UsernamePasswordAuthenticationToken
     */
    Authentication getAuthenticationToken(String username) throws UsernameNotFoundException {
        UserDetails userInfo = userDetailsService.loadUserByUsername(username)
        if (userInfo == null) {
            throw new UsernameNotFoundException("User Not Found username: $username")
        }
        def upauth = new UsernamePasswordAuthenticationToken(username, null, userInfo.authorities)
        upauth.authenticated = false
        return upauth
    }

}
