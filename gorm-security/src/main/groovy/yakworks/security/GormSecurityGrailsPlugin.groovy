/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.Plugin
import yakworks.security.audit.AuditStampBeforeValidateListener
import yakworks.security.audit.AuditStampPersistenceEventListener
import yakworks.security.audit.AuditStampSupport
import yakworks.security.audit.DefaultAuditUserResolver
import yakworks.security.gorm.AppUserService
import yakworks.security.gorm.PasswordValidator
import yakworks.security.gorm.model.AppUser
import yakworks.security.spring.AppUserDetailsService
import yakworks.security.spring.AsyncSecureService
import yakworks.security.spring.SpringSecService
import yakworks.security.spring.listeners.SecLoginHandler
import yakworks.security.spring.listeners.SecLogoutHandler
import yakworks.security.user.CurrentUserHolder

@SuppressWarnings(['Indentation'])
class GormSecurityGrailsPlugin extends Plugin {

    def loadAfter = ['spring-security-core', 'spring-security-ldap', 'spring-security-rest', 'gorm-tools', 'datasource']
    def pluginExcludes = ["**/init/**"]

    Closure doWithSpring() { { ->

        secService(SpringSecService, AppUser){ bean -> bean.lazyInit = true}
        userService(AppUserService){ bean -> bean.lazyInit = true}

        currentUser(CurrentUserHolder){ bean -> bean.lazyInit = true}

        def securityConf = SpringSecurityUtils.securityConfig
        if (securityConf.active) {

            asyncService(AsyncSecureService){ bean -> bean.lazyInit = true}
            passwordValidator(PasswordValidator){ bean -> bean.lazyInit = true}

            // spring security uses an older deprecated interface security.authentication.encoding.PasswordEncoder
            // this one wraps the new one in the old interface as spring sec's DaoAuthenticationProvider needs it
            // once thats upgraded then we can fix this
            passwordEncoder(BCryptPasswordEncoder){ bean -> bean.lazyInit = true}

            //overrrides the spring sec's userDetailsService
            userDetailsService(AppUserDetailsService){ bean -> bean.lazyInit = true}

            secLoginHandler(SecLoginHandler){ bean -> bean.lazyInit = true}
            secLogoutHandler(SecLogoutHandler){ bean -> bean.lazyInit = true}
            // authenticationDetailsSource(RallyAuthenticationDetailsSource)
        }

        //dont register beans if audit trail is disabled.
        if (config.getProperty('gorm.tools.audit.enabled', Boolean, true)) {
            // auditStampEventListener(AuditStampEventListener)
            auditStampBeforeValidateListener(AuditStampBeforeValidateListener)
            auditStampPersistenceEventListener(AuditStampPersistenceEventListener)
            auditStampSupport(AuditStampSupport)
            auditUserResolver(DefaultAuditUserResolver)
        }

    } }

    @Override
    void doWithApplicationContext() {
        // def persList = applicationContext.getBean('auditStampPersistenceEventListener', AuditStampPersistenceEventListener)
        // applicationContext.addApplicationListener(persList)
        // datastore.applicationEventPublisher.addApplicationListener(ctx.getBean("auditStampPersistenceEventListener"))

        // applicationContext.getBeansOfType(Datastore).each { String key, Datastore datastore ->
        //     println "key $key datastore $datastore"
        //     // def persList = applicationContext.getBean('auditStampPersistenceEventListener', AuditStampPersistenceEventListener)
        //     // assert persList
        //     def evPublisher = datastore.applicationEventPublisher
        //     evPublisher.addApplicationListener(new AuditStampPersistenceListener(datastore))
        // }

    }
}
