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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext;
import yakworks.security.config.SamlSecurityConfiguration;
import yakworks.security.config.SpringSecurityConfiguration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

/**
 * An example of explicitly configuring Spring Security with the defaults.
 *
 * @author Rob Winch
 */
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

        return http.build();
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


}
