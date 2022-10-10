/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.config

import groovy.transform.CompileStatic

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication

import yakworks.security.spring.user.SpringSamlUser
import yakworks.security.spring.user.SpringUserInfo

import static org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider.ResponseToken
import static org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter

/**
 * Converts the saml response to the Saml2Authentication.
 * This is a custom implementation to build our SpringSamlUser (which implements standard UserDetails and UserInfo)
 */
@CompileStatic
class SamlResponseConverter implements Converter<ResponseToken, Saml2Authentication> {

    UserDetailsService userDetailsService

    Converter<ResponseToken, Saml2Authentication> defaultConverter = createDefaultResponseAuthenticationConverter();

    SamlResponseConverter(UserDetailsService userDetailsService){
        this.userDetailsService = userDetailsService
    }

    @Override
    Saml2Authentication convert(ResponseToken responseToken) {
        Saml2Authentication authentication = defaultConverter.convert(responseToken);
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.principal
        //principal.name by default from Okta is the email.
        String username = principal.name
        //lookup existing User
        def springUser = (SpringUserInfo)userDetailsService.loadUserByUsername(username)
        //TODO This is where we can call out to create one.
        if (!springUser) {
            throw new UsernameNotFoundException("Saml authentication was successful but no application user found for username: $username")
        }
        // setup SpringSamlUser from principal, we keep SpringSamlUser as inheriting Saml2AuthenticatedPrincipal
        // so it more or less the same object. But we only use whats in the springUser from userDetails right now
        // essentially the only thing being used from Okta saml is the fact that they are authorized and the username (email?) for lookup
        // TODO see commented out groups POC below for how we can setup a user.
        SpringSamlUser springSamlUser = SpringSamlUser.of(principal, springUser)

        return new Saml2Authentication(springSamlUser, authentication.getSaml2Response(), springSamlUser.getAuthorities());
    }

    // TODO FUTURE USE TO PULL groups using Okta's spring saml example
    //  could config a certain group and if we see it come through then setup the user in our system for a mapped role.
    // List<String> groups = principal.getAttribute("groups");
    // Set<GrantedAuthority> authorities = new HashSet<>();
    // if (groups != null) {
    //     authorities.addAll(groups.collect{ new SimpleGrantedAuthority(it) })
    //     // groups.stream().map(group -> new SimpleGrantedAuthority(group)).forEach(authorities::add);
    // } else {
    //     authorities.addAll(authentication.getAuthorities());
    // }
}
