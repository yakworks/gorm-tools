/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.saml;

import groovy.transform.CompileStatic;

import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * NOT USED
 * Proof of Concept for using a SAML and Special User converter
 */
class SamlConfig {

    static void applySamlSecurityWithConverter(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        //as soon bean is setup then it tries to use it for everything instead of just this one so we do it without bean
        //need to sort out how to make it not do this.
        OpenSaml4AuthenticationProvider samlAuthenticationProvider = new OpenSaml4AuthenticationProvider();
        samlAuthenticationProvider.setResponseAuthenticationConverter(new SamlResponseConverter(userDetailsService));

        http
            .saml2Login(saml2 -> {
                saml2.authenticationManager(new ProviderManager(samlAuthenticationProvider))
                    .defaultSuccessUrl("/saml", true);
            })
            .saml2Logout(withDefaults());
    }

}
