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
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

import yakworks.openapi.gorm.OpenApiGenerator
import yakworks.rally.RallyConfiguration
import yakworks.rest.grails.AppInfoBuilder
import yakworks.security.spring.DefaultSecurityConfiguration
import yakworks.security.spring.token.CookieAuthSuccessHandler
import yakworks.security.spring.token.CookieBearerTokenResolver
import yakworks.security.spring.token.JwtTokenGenerator
import yakworks.security.spring.token.TokenUtils
import yakworks.security.spring.token.store.OpaqueTokenStoreAuthProvider
import yakworks.security.spring.token.store.TokenStore

import static org.springframework.security.config.Customizer.withDefaults

/**
 * An example of explicitly configuring Spring Security with the defaults.
 */
// keep componentScan in Application.groovy for now so unit test work. see notes in the TestSpringApplication class in tests
// @ComponentScan(['yakity.security', 'yakworks.security'])
@Lazy
@EnableWebSecurity //(debug = true)
@CompileStatic
@Configuration
@Import([RallyConfiguration])
class RallyApiConfiguration {

    @Value('${app.security.enabled:true}')
    boolean securityEnabled

    @Autowired(required = false) Saml2RelyingPartyProperties samlProps

    @Autowired JwtTokenGenerator tokenGenerator
    @Autowired CookieAuthSuccessHandler cookieAuthSuccessHandler
    @Autowired TokenStore tokenStorageService

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //defaults permit all
        List permitAllMatchers = [
            "/actuator/**",
            "/resources/**",
            // "/security-tests/error500",
            // "/security-tests/error400",
            "/security-tests/**",
            "/api/login",
            "/api/token",
            "/about"]

        if(!securityEnabled){
            //permit all wildcard
            permitAllMatchers << "/**"
        }

        http
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/security-tests/error401").authenticated()
                .requestMatchers("/security-tests/error403").hasRole("SUPER_DUPER")
                .requestMatchers(permitAllMatchers as String[]).permitAll()
                .anyRequest().authenticated()
            )
            // enable basic auth
            .httpBasic(withDefaults())
            // add default form for testing in browser
            // .formLogin(withDefaults())
            .formLogin( formLoginCustomizer ->
                formLoginCustomizer.successHandler(cookieAuthSuccessHandler)
            )
            //make stateless so no session stored on server
            .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            //remove the cookie on logout
            .logout().deleteCookies(TokenUtils.COOKIE_NAME);

        // Uncomment to enable SAML, will hit the metadata-uri on startup and fail if not found
        // TODO need to find a way to not hit server until its needed instead of on startup
        if(samlProps?.registration?.containsKey('okta')){
            DefaultSecurityConfiguration.applySamlSecurity(http, cookieAuthSuccessHandler)
        }

        http.oauth2Login(withDefaults())

        DefaultSecurityConfiguration.addJsonAuthenticationFilter(http)
        DefaultSecurityConfiguration.applyOauthJwt(http)

        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class)
        authenticationManagerBuilder.authenticationProvider(new OpaqueTokenStoreAuthProvider(tokenStorageService))

        return http.build()
    }

    // @Bean
    // AuthenticationManager authManager(HttpSecurity http) throws Exception {
    //     AuthenticationManagerBuilder authenticationManagerBuilder =
    //         http.getSharedObject(AuthenticationManagerBuilder.class);
    //     authenticationManagerBuilder.authenticationProvider(new CustomAuthenticationProvider());
    //     return authenticationManagerBuilder.build();
    // }

    @Bean
    CookieBearerTokenResolver bearerTokenResolver(){
        new CookieBearerTokenResolver()
    }

    // @Bean
    // CustomAuthenticationProvider customAuthenticationProvider(){
    //     new CustomAuthenticationProvider()
    // }

    // @Bean
    // @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    // KeyPair rsaKeyPair() {
    //     KeyPairUtils.generateRsaKey()
    // }

    @Bean
    AppInfoBuilder appInfoBuilder() {
        return new AppInfoBuilder()
    }

    @Bean
    OpenApiGenerator openApiGenerator() {
        def oag = new OpenApiGenerator()
        oag.apiSrc = 'api-docs/openapi'
        oag.apiBuild = 'api-docs/dist/openapi'
        oag.namespaceList = ['rally']
        return oag
    }

}
