/*
* Copyright 2013-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.rest.token

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException

import grails.gorm.transactions.Transactional
import yakworks.security.gorm.model.AppUserToken
import yakworks.security.spring.token.store.TokenStorageService

/**
 * GORM implementation for token storage. It will look for tokens on the DB using a domain class that will contain the
 * generated token and the username associated.
 * NOTE: The PostgresTokenStorageService is prefered over this since it encrypts the token, this is here more for H2 testing and potential
 * other databases.
 *
 * Once the username is found, it will delegate to the configured {@link UserDetailsService} for obtaining authorities
 * information.
 */
@Slf4j
@CompileStatic
class GormTokenStorageService implements TokenStorageService {

    @Autowired UserDetailsService userDetailsService

    UserDetails loadUserByToken(String tokenValue) throws OAuth2IntrospectionException {
        log.debug "Finding token ${tokenValue} in GORM"
        String username = findUsernameForExistingToken(tokenValue)
        if (username) {
            return userDetailsService.loadUserByUsername(username)
        }
        throw new BadOpaqueTokenException("Token ${tokenValue} not found")
    }

    @Transactional
    void storeToken(String tokenValue, UserDetails principal) {
        // log.debug "Storing principal for token: ${tokenValue}"
        log.debug "Storing principal for token: ${principal}"
        def newTokenObject = new AppUserToken(tokenValue: tokenValue, username: principal.username)
        newTokenObject.persist(flush: true)
    }

    @Transactional
    void removeToken(String tokenValue) throws OAuth2IntrospectionException {
        def existingToken = AppUserToken.findWhere(tokenValue: tokenValue)
        if (existingToken) {
            existingToken.remove()
        } else {
            throw new BadOpaqueTokenException("Token not found")
        }

    }

    @Transactional
    String findUsernameForExistingToken(String tokenValue) {
        log.debug "Searching in GORM for UserDetails of token"
        return AppUserToken.findWhere(tokenValue: tokenValue)?.username
    }

}
