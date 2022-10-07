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

import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import yakity.security.token.JwtTokenGenerator
import yakworks.security.config.SpringSecurityConfiguration

/**
 * An example of explicitly configuring Spring Security with the defaults.
 */
// keep componentScan in Application.groovy for now so test work. see notes in the TestSpringApplication class in tests
// @ComponentScan(['yakity.security', 'yakworks.security'])
@Lazy
@EnableWebSecurity(debug = true)
@CompileStatic
@Configuration
class AppSecurityConfiguration {

    @Autowired(required = false) Saml2RelyingPartyProperties samlProps
    @Autowired(required = false) ObjectMapper objectMapper

    @Component
    @ConfigurationProperties(prefix="app.security.jwt")
    static class JwtProperties {

        RSAPublicKey publicKey
        RSAPrivateKey privateKey

        long expiry = 60L
        String issuer = "self"

    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        SpringSecurityConfiguration.applyHttpSecurity(http)
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

        def jsonUnameFilter = new JsonUsernamePasswordLoginFilter(objectMapper)
        jsonUnameFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/login", "POST"))
        jsonUnameFilter.setAuthenticationSuccessHandler(new ForwardAuthenticationSuccessHandler("/token"))
        http.addFilterAfter(jsonUnameFilter, BasicAuthenticationFilter)

        // def restUnameFilter = new JsonLoginUserPasswordFilter(objectMapper)
        // http.addFilterAfter(restUnameFilter, BasicAuthenticationFilter)

        def builtChain =  http.build();
        //do after build as need to set the AuthenticationManager
        def authManagerAfter = http.getSharedObject(AuthenticationManager.class)
        jsonUnameFilter.setAuthenticationManager(authManagerAfter)

        return builtChain
    }
        // // Added *ONLY* to display the dbConsole.
        // // Best not to do this in production.  If you need frames, it would be best to use
        // //     http.headers().frameOptions().addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN));
        // // or in Spring Security 4, changing .disable() to .sameOrigin()
        // http.headers().frameOptions().disable()

        // // Again, do not do this in production unless you fully understand how to mitigate Cross-Site Request Forgery
        // // https://www.owasp.org/index.php/Cross-Site_Request_Forgery_%28CSRF%29_Prevention_Cheat_Sheet
        // http.csrf().disable()

    // @Bean
    // public InMemoryUserDetailsManager userDetailsService() {
    //     UserDetails user = User.withDefaultPasswordEncoder()
    //         .username("user")
    //         .password("123")
    //         .roles("USER")
    //         .build();
    //     return new InMemoryUserDetailsManager(user);
    // }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
        return NimbusJwtDecoder.withPublicKey(jwtProperties.publicKey).build();
    }

    @Bean
    JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
        JWK jwk = new RSAKey.Builder(jwtProperties.publicKey).privateKey(jwtProperties.privateKey).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(jwks);
        return encoder
    }

    @Bean
    JwtTokenGenerator tokenGenerator(){
        new JwtTokenGenerator()
    }


}
