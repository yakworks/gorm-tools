/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.user

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.core.AuthenticatedPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
 * Listener to update `authentication.details` in the Authentication with common UserInfo object.
 * Normally Spring Sec just stores an object with ip address and sessionId, so we replace that with a more generic User object.
 * By storing it in details instead of principal trying to mess around with the principal we only have one place to do it.
 * The only thing stored in details by default is the ip adresss and sessionId which we just proxy in the SpringUser.
 * Also makes it easy to override for customer funtionanlity by simply implementing you own listener.
 */
@Slf4j
@CompileStatic
class AuthSuccessUserInfoListener {

    @Autowired UserDetailsService userDetailsService

    @EventListener
    void onSuccess(AuthenticationSuccessEvent success) {
        AbstractAuthenticationToken authentication = (AbstractAuthenticationToken)success.authentication
        Object principal = authentication.principal

        log.debug("😀😀😀 SUCCESS authentication:${authentication.class.name} principal: ${principal.class.name}", success.authentication)
        //principal will be one of 2 bases in stock spring sec. UserDetails if its standard filter like UserNamePassword or BasicAuth
        //most often these would have gone through DaoAuthenticationProvider and UserDetailsService.
        //if its an AuthenticatedPrincipal then its Oauth or Saml.
        if (principal instanceof UserDetails) {
            doUserDetails(authentication, principal)
        }
        else {
            doIdentityProvided(authentication, principal)
        }

    }

    /**
     * Does the UserDetails. We already implement the UserDetailsService there
     */
    void doUserDetails(AbstractAuthenticationToken authentication, UserDetails principal){
        //its already and instance of SpringUserInfo then just set its details and replace it
        if(principal instanceof SpringUserInfo){
            principal.setAuditDetails(authentication.details)
            //replace with this springUserInfo
            authentication.setDetails(principal)
        }

    }

    void doIdentityProvided(AbstractAuthenticationToken authentication, Object principal){
        //if its an instance of AuthenticatedPrincipal then use it
        String username
        if (principal instanceof AuthenticatedPrincipal) {
            username = principal.name
        }
        else { //could be JWT so use the name in the authentication to do the lookup
            username = authentication.name
        }

        def springUser = userDetailsService.loadUserByUsername(username)
        if (!springUser) {
            //TODO This is where we can call out to create one if it doesn't exist?
            throw new UsernameNotFoundException("User Not Found username: $username")
        }
        if(springUser instanceof SpringUserInfo){
            springUser.setAuditDetails(authentication.details)
        }
        //replace with this springUserInfo
        authentication.setDetails(springUser)

        // if(principal instanceof Saml2AuthenticatedPrincipal){
        //     log.debug("😀😀😀 SAML LOGIN")
        //     Saml2AuthenticatedPrincipal samlPrincipal = (Saml2AuthenticatedPrincipal) principal
        // }
        //FUTURE USE
    }

}
