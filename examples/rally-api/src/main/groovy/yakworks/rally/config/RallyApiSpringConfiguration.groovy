/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.config

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.SecurityFilterChain

import yakworks.gorm.api.support.QueryArgsValidator
import yakworks.openapi.gorm.OpenApiGenerator
import yakworks.rally.RallyConfiguration
import yakworks.rally.api.TestTimeoutQueryArgsValidator
import yakworks.rest.grails.AppInfoBuilder
import yakworks.security.spring.DefaultSecurityConfiguration
import yakworks.security.spring.PermissionsAuthorizationManager
import yakworks.security.spring.token.CookieAuthSuccessHandler
import yakworks.security.spring.token.CookieUrlTokenSuccessHandler
import yakworks.security.spring.token.TokenUtils
import yakworks.security.spring.token.generator.JwtTokenGenerator
import yakworks.security.spring.token.store.TokenStore

import static org.springframework.security.config.Customizer.withDefaults

/**
 * An example of explicitly configuring Spring Security with the defaults.
 */
@EnableMethodSecurity
@EnableWebSecurity //(debug = true)
@CompileStatic
@Configuration
@Import([RallyConfiguration])
class RallyApiSpringConfiguration {

    @Value('${app.security.enabled:true}')
    boolean securityEnabled

    @Value('${app.security.frontendCallbackUrl:""}')
    String frontendCallbackUrl

    @Autowired(required = false) Saml2RelyingPartyProperties samlProps

    @Autowired JwtTokenGenerator tokenGenerator
    @Autowired CookieAuthSuccessHandler cookieAuthSuccessHandler
    @Autowired CookieUrlTokenSuccessHandler cookieUrlTokenSuccessHandler
    @Autowired TokenStore tokenStore
    @Autowired UserDetailsService userDetailsService

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, PermissionsAuthorizationManager permissionsAuthorizationManager) throws Exception {
        //var sth = new SubjectThreadState(null);
        //defaults permit all
        List permitAllMatchers = [
            "/actuator/**",
            "/resources/**",
            // "/security-tests/error500",
            // "/security-tests/error400",
            "/security-tests/**",
            "/login*",
            "/token",
            '/oauth/token',
            "/about",
            "/rally/smoke/**"
        ]

        if(!securityEnabled){
            //permit all wildcard
            permitAllMatchers << "/**"
        }

        http
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/security-tests/error401").authenticated()
                .requestMatchers("/security-tests/error403").hasRole("SUPER_DUPER")
                .requestMatchers(permitAllMatchers as String[]).permitAll()
                .requestMatchers("/validate").authenticated()
                .anyRequest().access(permissionsAuthorizationManager)
            )
            // http basic auth
            .httpBasic(withDefaults())
            // add default form for testing in browser
            .formLogin( form -> {
                //adds success handler for adding cookie
                // form.loginPage("/login.html")
                form.loginProcessingUrl("/perform_login")
                form.successHandler(cookieAuthSuccessHandler)
            })
            //make stateless so no session stored on server
            .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            //remove the cookie on logout
            .logout()
                .deleteCookies(TokenUtils.COOKIE_NAME)
                .clearAuthentication(true).invalidateHttpSession(true)

        // Uncomment to enable SAML, will hit the metadata-uri on startup and fail if not found
        // TODO need to find a way to not hit server until its needed instead of on startup
        if(samlProps?.registration?.containsKey('okta')){
            //adds success handler for adding cookie
            DefaultSecurityConfiguration.applySamlSecurity(http, cookieUrlTokenSuccessHandler)
        }

        http.oauth2Login(withDefaults())

        // Legacy tokenLegacy for using username/password json in api/login, forwards to api/tokenLegacy which saves random string in db as token
        // DefaultSecurityConfiguration.addJsonAuthenticationFilter(http, tokenStore)

        //enables jwt and oauth
        DefaultSecurityConfiguration.applyOauthJwt(http, userDetailsService)
        DefaultSecurityConfiguration.addOpaqueTokenSupport(http, tokenStore)

        return http.build()
    }

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

    @Bean
    QueryArgsValidator queryArgsValidator() {
        return new TestTimeoutQueryArgsValidator()
    }

    // @Bean @Lazy(false)
    // WebMvcRegistrations webMvcRegistrations() {
    //     return new WebMvcRegistrations() {
    //
    //         @Override
    //         public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
    //             RequestMappingHandlerMapping handlerMapping = new CustomRequestMappingHandlerMapping();
    //             //handlerMapping.setUseTrailingSlashMatch(false);
    //             return handlerMapping;
    //         }
    //
    //         @Override
    //         public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
    //             RequestMappingHandlerAdapter handlerAdapter = new CustomRequestMappingHandlerAdapter();
    //             //handlerMapping.setUseTrailingSlashMatch(false);
    //             return handlerAdapter;
    //         }
    //
    //     };
    // }


}
