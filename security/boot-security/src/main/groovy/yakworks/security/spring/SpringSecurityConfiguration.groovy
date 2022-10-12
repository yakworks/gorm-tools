/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import java.security.KeyPair
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

import groovy.transform.CompileStatic

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Role
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import yakworks.security.SecService
import yakworks.security.services.PasswordValidator
import yakworks.security.spring.token.JwtTokenGenerator
import yakworks.security.spring.user.AuthSuccessUserInfoListener
import yakworks.security.user.CurrentUser
import yakworks.security.user.CurrentUserHolder

import static org.springframework.security.config.Customizer.withDefaults

@Configuration //(proxyBeanMethods = false)
@Lazy
@CompileStatic
@EnableConfigurationProperties([JwtProperties])
class SpringSecurityConfiguration {

    static void applyHttpSecurity(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/actuator/**", "/resources/**", "/about").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults())
            .formLogin(withDefaults())
            // .formLogin( formLoginCustomizer ->
            //     formLoginCustomizer.defaultSuccessUrl("/", true)
            // )

        def ctx = http.getSharedObject(ApplicationContext.class)
        //POC for enabling the legacy login with a POST to the /api/login endpoint.
        def jsonUnameFilter = new JsonUsernamePasswordLoginFilter(ctx.getBean(ObjectMapper))
        jsonUnameFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/login", "POST"))
        jsonUnameFilter.setAuthenticationSuccessHandler(new ForwardAuthenticationSuccessHandler("/token"))
        jsonUnameFilter.setAuthenticationManager(authenticationManager)
        http.addFilterAfter(jsonUnameFilter, BasicAuthenticationFilter)


    }

    static void applySamlSecurity(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
            .saml2Login(withDefaults())
            .saml2Logout(withDefaults());
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        //this gets the default authManager from the the authConfig which gets injected during the autoconfig securityFilterChain process
        return authConfig.getAuthenticationManager()
    }

    @Bean
    AuthSuccessUserInfoListener authSuccessUserInfoListener(){
        new AuthSuccessUserInfoListener()
    }

    //defaults
    @Bean
    @ConditionalOnMissingBean([ SecurityFilterChain.class ])
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        applyHttpSecurity(http, authenticationManager)
        return http.build()
    }

    @Bean @ConditionalOnMissingBean
    SecService secService(){
        new SpringSecService()
    }

    @Bean('${CurrentUserHolder.name}')
    CurrentUserHolder CurrentUserHolder(){
        //here just to set the static, there a better way?
        new CurrentUserHolder()
    }

    @Bean @ConditionalOnMissingBean
    CurrentUser currentUser(){
        new CurrentSpringUser()
    }

    @Bean @ConditionalOnMissingBean
    PasswordValidator passwordValidator(){
        new PasswordValidator()
    }

    @Bean
    @ConditionalOnMissingBean
    PasswordEncoder passwordEncoder(){
        new BCryptPasswordEncoder()
    }

    @Configuration @Lazy
    static class JwtTokenConfiguration {

        @Bean @ConditionalOnMissingBean
        @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
        KeyPair rsaKeyPair(JwtProperties jwtProperties) {
            new KeyPair(jwtProperties.publicKey, jwtProperties.privateKey)
        }

        @Bean @ConditionalOnMissingBean
        JwtDecoder jwtDecoder(KeyPair rsaKeyPair) {
            return NimbusJwtDecoder.withPublicKey((RSAPublicKey)rsaKeyPair.public).build();
        }

        @Bean @ConditionalOnMissingBean
        JwtEncoder jwtEncoder(KeyPair rsaKeyPair) {
            JWK jwk = new RSAKey.Builder((RSAPublicKey)rsaKeyPair.public)
                .privateKey((RSAPrivateKey)rsaKeyPair.private)
                .build();
            JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
            NimbusJwtEncoder encoder = new NimbusJwtEncoder(jwks);
            return encoder
        }

        @Bean @ConditionalOnMissingBean
        JwtTokenGenerator tokenGenerator(){
            new JwtTokenGenerator()
        }

    }

    // @Configuration //(proxyBeanMethods = false)
    // @Lazy
    // @ConditionalOnProperty("spring.security.saml2.relyingparty.registration.okta.assertingparty.metadata-uri")
    // @CompileStatic
    // static class SamlSecurityConfiguration implements ApplicationContextAware {
    //
    //     ApplicationContext applicationContext
    // }

}
