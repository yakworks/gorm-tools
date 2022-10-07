/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.config

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider

import yakworks.security.spring.user.UserInfoDetailsService

import static org.springframework.security.config.Customizer.withDefaults

@Configuration //(proxyBeanMethods = false)
@Lazy
@CompileStatic
class SamlSecurityConfiguration implements ApplicationContextAware {

    ApplicationContext applicationContext

    //as soon bean is setup then it tries to use it for everything
    //AuthenticationManagerBuilder's authenticationProviders get set from any beans that exist and that halts the part
    // in InitializeAuthenticationProviderManagerConfigurer that setup the default DaoAuthenticationProvider.
    // so we ether dont setup a DaoAuthenticationProvider bean or we dont set this up as a bean
    // @Bean
    // OpenSaml4AuthenticationProvider samlAuthenticationProvider(UserInfoDetailsService userDetailsService){
    //     OpenSaml4AuthenticationProvider samlAuthenticationProvider = new OpenSaml4AuthenticationProvider();
    //     samlAuthenticationProvider.setResponseAuthenticationConverter(new SamlResponseConverter(userDetailsService));
    //     return samlAuthenticationProvider
    // }

    static void applyHttpSecurity(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        //as soon bean is setup then it tries to use it for everything instead of just this one so we do it without bean
        //need to sort out how to make it not do this.
        OpenSaml4AuthenticationProvider samlAuthenticationProvider = new OpenSaml4AuthenticationProvider();
        samlAuthenticationProvider.setResponseAuthenticationConverter(new SamlResponseConverter(userDetailsService));

        http
            .saml2Login(saml2 -> saml2
                .authenticationManager(new ProviderManager(samlAuthenticationProvider))
                .defaultSuccessUrl("/saml", true)
            )
            .saml2Logout(withDefaults());
    }

}
