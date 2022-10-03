/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.config


import groovy.transform.CompileStatic

import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

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

@Configuration //(proxyBeanMethods = false)
@Lazy
//@EnableConfigurationProperties([AsyncConfig, GormConfig, IdGeneratorConfig])
@CompileStatic
class SpringSecurityConfiguration implements ApplicationContextAware, BeanFactoryAware {

    BeanFactory beanFactory
    ApplicationContext applicationContext

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
