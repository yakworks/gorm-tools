/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.config


import groovy.transform.CompileStatic

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

import yakworks.security.SecService
import yakworks.security.gorm.AppUserService
import yakworks.security.gorm.PasswordValidator
import yakworks.security.spring.CurrentSpringUser
import yakworks.security.spring.SpringSecService
import yakworks.security.spring.user.AppUserDetailsService
import yakworks.security.spring.user.UserInfoDetailsService
import yakworks.security.testing.SecuritySeedData
import yakworks.security.user.CurrentUser
import yakworks.security.user.CurrentUserHolder

@Configuration //(proxyBeanMethods = false)
@Lazy
//@EnableConfigurationProperties([AsyncConfig, GormConfig, IdGeneratorConfig])
@CompileStatic
class SpringSecurityConfiguration {

    @Bean
    SecuritySeedData securitySeedData(){
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

    // @Bean
    // PasswordEncoder passwordEncoder(){
    //     new BCryptPasswordEncoder()
    // }

}
