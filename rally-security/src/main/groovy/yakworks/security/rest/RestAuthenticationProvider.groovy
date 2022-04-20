/*
* Copyright 2013-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.rest

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert

import com.nimbusds.jwt.JWT
import grails.plugin.springsecurity.rest.JwtService
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.generation.jwt.AbstractJwtTokenGenerator
import grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService

/**
 * Replaces the stock one to check and see if its a JWT token or an access token store in the db.
 * Authenticates a request based on the token passed. This is called by {@link grails.plugin.springsecurity.rest.RestTokenValidationFilter}.
 */
@SuppressWarnings('ClassNameSameAsSuperclass')
@Slf4j
@CompileStatic
class RestAuthenticationProvider extends grails.plugin.springsecurity.rest.RestAuthenticationProvider {

    @Autowired TokenStorageService tokenStorageService
    @Autowired JwtService jwtService

    Boolean useJwt

    /**
     * Returns an authentication object based on the token value contained in the authentication parameter. To do so,
     * it uses a {@link TokenStorageService}.
     * @throws AuthenticationException
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug "Use JWT: ${useJwt}"
        Assert.isInstanceOf(AccessToken, authentication, "Only AccessToken is supported")
        AccessToken authenticationRequest = authentication as AccessToken
        AccessToken authenticationResult = new AccessToken(authenticationRequest.accessToken)

        if (authenticationRequest.accessToken) {
            log.debug "Trying to validate token ${authenticationRequest.accessToken}"
            UserDetails userDetails
            boolean isJwtToken = false
            if((authenticationRequest.getDetails() as String) == 'jwt'){
                isJwtToken = true
                // TODO implement this part for jwt
                userDetails = tokenStorageService.loadUserByToken(authenticationRequest.accessToken) as UserDetails
            } else {
                //then its stored and should be looked up
                // TODO implement this part
                userDetails = tokenStorageService.loadUserByToken(authenticationRequest.accessToken) as UserDetails
            }

            Integer expiration = null
            JWT jwt = null
            if (useJwt || isJwtToken) {
                Date now = new Date()
                jwt = jwtService.parse(authenticationRequest.accessToken)

                // Prevent refresh tokens from being used for authentication
                if (jwt.JWTClaimsSet.getBooleanClaim(AbstractJwtTokenGenerator.REFRESH_ONLY_CLAIM)) {
                    throw new TokenNotFoundException("Token ${authenticationRequest.accessToken} is not valid")
                }

                Date expiry = jwt.JWTClaimsSet.expirationTime
                if (expiry) {
                    log.debug "Now is ${now} and token expires at ${expiry}"

                    TimeDuration timeDuration = TimeCategory.minus(expiry, now)
                    expiration = Math.round((timeDuration.toMilliseconds() / 1000) as float)
                    log.debug "Expiration: ${expiration}"
                }
            }

            authenticationResult = new AccessToken(userDetails, userDetails.authorities, authenticationRequest.accessToken, null, expiration, jwt, null)
            log.debug "Authentication result: {}", authenticationResult
        }

        return authenticationResult
    }

    boolean supports(Class<?> authentication) {
        return AccessToken.isAssignableFrom(authentication)
    }
}
