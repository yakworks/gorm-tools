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
package yakworks.rally.api


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

import yakworks.rally.RallyConfiguration
import yakworks.security.spring.DefaultSecurityConfiguration

import static org.springframework.security.config.Customizer.withDefaults

/**
 * An example of explicitly configuring Spring Security with the defaults.
 */
// keep componentScan in Application.groovy for now so test work. see notes in the TestSpringApplication class in tests
// @ComponentScan(['yakity.security', 'yakworks.security'])
@Lazy
@EnableWebSecurity //(debug = true)
@CompileStatic
@Configuration
@Import([RallyConfiguration])
class RallyApiConfiguration {

    @Autowired(required = false) Saml2RelyingPartyProperties samlProps

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // DefaultSecurityConfiguration.applyBasicDefaults(http)
        http
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/api/**", "/actuator/**", "/resources/**", "/about").permitAll()
                .anyRequest().authenticated()
            )
        // enable basic auth
            .httpBasic(withDefaults())
        // add default form for testing in browser
            .formLogin(withDefaults());

        // Uncomment to enable SAML, will hit the metadata-uri on startup and fail if not found
        // TODO need to find a way to not hit server until its needed instead of on startup
        if(samlProps?.registration?.containsKey('okta')){
            DefaultSecurityConfiguration.applySamlSecurity(http)
        }

        DefaultSecurityConfiguration.addJsonAuthenticationFilter(http);
        DefaultSecurityConfiguration.applyOauthJwt(http);

        return http.build()
    }

    // @Bean
    // @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    // KeyPair rsaKeyPair() {
    //     KeyPairUtils.generateRsaKey()
    // }
}
