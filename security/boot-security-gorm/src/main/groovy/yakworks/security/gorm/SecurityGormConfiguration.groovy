/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm

import groovy.transform.CompileStatic

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.core.userdetails.UserDetailsService

import yakworks.security.rest.token.GormTokenStorageService

@ComponentScan('yakworks.security.gorm.model') //here to pick up the Repos
@Configuration //(proxyBeanMethods = false)
@Lazy
@CompileStatic
class SecurityGormConfiguration {

    // @Bean @ConditionalOnMissingBean
    // PasswordValidator passwordValidator(){
    //     new PasswordValidator()
    // }

    @Bean @ConditionalOnMissingBean
    AppUserService userService(){
        new AppUserService()
    }


    @Bean
    @ConditionalOnMissingBean
    UserDetailsService userDetailsService(){
        new AppUserDetailsService()
    }

    @Bean
    AsyncSecureService asyncService(){
        new AsyncSecureService()
    }

    @Bean
    GormTokenStorageService tokenStorageService(){
        new GormTokenStorageService()
    }


}
