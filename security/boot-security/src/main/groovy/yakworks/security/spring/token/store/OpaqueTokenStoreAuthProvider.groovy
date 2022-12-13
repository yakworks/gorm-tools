/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token.store

import java.time.Instant

import groovy.transform.CompileStatic

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector

/**
 * Adds AuthenticationProvider to the chain
 * This looks up the "Opaque" token in the DB and authenticates if its good.
 * We start the opaque tokens with "yak" by default but this can be configured in properties
 */
@CompileStatic
class OpaqueTokenStoreAuthProvider implements AuthenticationProvider {
    private static final String AUTHORITY_PREFIX = "SCOPE_";

    TokenStorageService tokenStorageService
    /** the prefix of the token if its an opaque one */
    String tokenPrefix = 'yak'

    OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider

    OpaqueTokenStoreAuthProvider(TokenStorageService tokenStorageService){
        this.tokenStorageService = tokenStorageService
        // StoreOpaqueTokenIntrospector introspec = new StoreOpaqueTokenIntrospector()
        opaqueTokenAuthenticationProvider = new OpaqueTokenAuthenticationProvider(this::introspect)
        // opaqueTokenAuthenticationProvider.authenticationConverter = OpaqueTokenStoreAuthProvider::convert
    }

    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
        assert bearer
        String token = bearer.token
        if(token.startsWith(tokenPrefix)){
            //send to delegate
            return opaqueTokenAuthenticationProvider.authenticate(authentication)
        } else {
            return null
        }
    }

    OAuth2AuthenticatedPrincipal introspect(String token) {
        UserDetails udet = tokenStorageService.loadUserByToken(token)
        if(!udet) throw new BadOpaqueTokenException("Provided token isn't active");

        return new DefaultOAuth2AuthenticatedPrincipal(
            udet.username,
            [name: udet.username] as Map<String, Object>,
            [new SimpleGrantedAuthority(AUTHORITY_PREFIX + "admin")] as Collection<GrantedAuthority>
        )
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
    //
    // static BearerTokenAuthentication convert(String introspectedToken,
    //                                          OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
    //
    //     if(!authenticatedPrincipal) return null
    //
    //     //below is copy from convert in OpaqueTokenAuthenticationProvider
    //     Instant iat = authenticatedPrincipal.getAttribute(OAuth2TokenIntrospectionClaimNames.IAT);
    //     Instant exp = authenticatedPrincipal.getAttribute(OAuth2TokenIntrospectionClaimNames.EXP);
    //     OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, introspectedToken,
    //         iat, exp);
    //     return new BearerTokenAuthentication(authenticatedPrincipal, accessToken,
    //         authenticatedPrincipal.getAuthorities());
    // }
}
