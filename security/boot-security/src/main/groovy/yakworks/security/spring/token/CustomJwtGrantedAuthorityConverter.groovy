/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Used in JwtAuthenticationConverter and will load authorities from db instead of taking it from jwt token
 * so that it will work with permissions, and will always have upto date authorities.
 */
@CompileStatic
class CustomJwtGrantedAuthorityConverter  implements Converter<Jwt, Collection<GrantedAuthority>> {

    UserDetailsService userDetailsService

    CustomJwtGrantedAuthorityConverter(UserDetailsService userDetailsService){
        this.userDetailsService = userDetailsService
    }

    @Override
    Collection<GrantedAuthority> convert(Jwt source) {
        String username = source.getSubject()
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return userDetails.authorities as Collection<GrantedAuthority>
    }
}
