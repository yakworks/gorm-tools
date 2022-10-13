/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package yakity.security

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.SecurityFilterChain

import yakworks.security.spring.SpringSecurityConfiguration

/**
 * An example of explicitly configuring Spring Security with the defaults.
 */
// keep componentScan in Application.groovy for now so test work. see notes in the TestSpringApplication class in tests
//@ComponentScan(['yakity.security', 'yakworks.security'])
@Lazy
@EnableWebSecurity //(debug = true)
@CompileStatic
@Configuration
@Import([SpringSecurityConfiguration])
class AppSecurityConfiguration {

    @Autowired(required = false) Saml2RelyingPartyProperties samlProps

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService, AuthenticationManager authenticationManager) throws Exception {
        SpringSecurityConfiguration.applyHttpSecurity(http, authenticationManager)
        if(samlProps?.registration?.containsKey('okta')){
            SpringSecurityConfiguration.applySamlSecurity(http, userDetailsService)
        }

        //JWT
        // http.csrf((csrf) -> csrf.ignoringAntMatchers("/token"))
        http.csrf().disable()
            .oauth2ResourceServer((oauthServer) ->
                oauthServer.jwt()
            )
        // .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // .exceptionHandling((exceptions) -> exceptions
        // 		.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
        // 		.accessDeniedHandler(new BearerTokenAccessDeniedHandler())
        // );

        return http.build()
    }
    // // Added *ONLY* to display the dbConsole.
    // // Best not to do this in production.  If you need frames, it would be best to use
    // //     http.headers().frameOptions().addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN));
    // // or in Spring Security 4, changing .disable() to .sameOrigin()
    // http.headers().frameOptions().disable()

    // @Bean
    // AuthenticationEvents authenticationEvents(){
    //     new AuthenticationEvents()
    // }

}
