/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.config


import groovy.transform.CompileStatic

import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider
import org.springframework.security.web.SecurityFilterChain

import yakworks.security.SecService
import yakworks.security.audit.AuditStampBeforeValidateListener
import yakworks.security.audit.AuditStampPersistenceEventListener
import yakworks.security.audit.AuditStampSupport
import yakworks.security.audit.DefaultAuditUserResolver
import yakworks.security.gorm.AppUserService
import yakworks.security.gorm.PasswordValidator
import yakworks.security.spring.CurrentSpringUser
import yakworks.security.spring.SpringSecService
import yakworks.security.spring.user.AppUserDetailsService
import yakworks.security.spring.user.UserInfoDetailsService
import yakworks.security.testing.SecuritySeedData
import yakworks.security.user.CurrentUser
import yakworks.security.user.CurrentUserHolder

import static org.springframework.security.config.Customizer.withDefaults

@Configuration //(proxyBeanMethods = false)
@Lazy
@CompileStatic
class SpringSecurityConfiguration implements ApplicationContextAware, BeanFactoryAware {

    BeanFactory beanFactory
    ApplicationContext applicationContext

    static void applyHttpSecurity(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorize) ->
                authorize.mvcMatchers("/actuator/**", "/resources/**", "/about").permitAll()
                    .anyRequest().authenticated()
            )
            .httpBasic(withDefaults())
        // .formLogin(withDefaults())
            .formLogin( formLoginCustomizer ->
                formLoginCustomizer.defaultSuccessUrl("/", true)
            )
    }

    static void applySamlSecurity(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
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


    //defaults
    @Bean
    @ConditionalOnMissingBean([ SecurityFilterChain.class ])
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        applyHttpSecurity(http)
        return http.build()
    }

    @Bean
    SecuritySeedData securitySeedData(){
        List<String> packageNames = AutoConfigurationPackages.get(this.beanFactory)
        new SecuritySeedData()
    }

    @Bean
    SecService secService(){
        new SpringSecService()
    }

    @Bean
    AppUserService userService(){
        new AppUserService()
    }

    @Bean('${CurrentUserHolder.name}')
    CurrentUserHolder CurrentUserHolder(){
        //here just to set the static, there a better way?
        new CurrentUserHolder()
    }

    @Bean
    CurrentUser currentUser(){
        new CurrentSpringUser()
    }

    //FIXME done in plugin until we figure out order here in java config
    // @Bean
    // AsyncSecureService asyncService(){
    //     new AsyncSecureService()
    // }

    @Bean
    PasswordValidator passwordValidator(){
        new PasswordValidator()
    }

    @Bean
    UserInfoDetailsService userDetailsService(){
        new AppUserDetailsService()
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        new BCryptPasswordEncoder()
    }

    //dont register beans if audit trail is disabled.
    @ConditionalOnProperty(value="gorm.tools.audit.enabled", havingValue = "true", matchIfMissing = true)
    @Configuration @Lazy
    static class AuditStampConfiguration {
        @Bean
        AuditStampBeforeValidateListener auditStampBeforeValidateListener(){
            new AuditStampBeforeValidateListener()
        }
        @Bean
        AuditStampPersistenceEventListener auditStampPersistenceEventListener(){
            new AuditStampPersistenceEventListener()
        }
        @Bean
        AuditStampSupport auditStampSupport(){
            new AuditStampSupport()
        }
        @Bean
        DefaultAuditUserResolver auditUserResolver(){
            new DefaultAuditUserResolver()
        }

    }
}
