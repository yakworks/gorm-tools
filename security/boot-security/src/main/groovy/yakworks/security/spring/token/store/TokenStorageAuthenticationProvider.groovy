/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token.store

import groovy.transform.CompileStatic

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken

/**
 * Adds AuthenticationProvider to the chain
 * This looks up the token in the db and authenticates if its good.
 */
@CompileStatic
class TokenStorageAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
        // Jwt jwt = getJwt(bearer);
        // AbstractAuthenticationToken token = this.jwtAuthenticationConverter.convert(jwt);
        // token.setDetails(bearer.getDetails());
        // this.logger.debug("Authenticated token");

        //throw new CredentialsExpiredException("Storage token")

        return null
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
