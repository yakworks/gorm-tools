/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import gorm.tools.audit.AuditStampBeforeValidateListener
import gorm.tools.audit.AuditStampPersistenceEventListener
import gorm.tools.audit.AuditStampSupport
import gorm.tools.security.domain.AppUser
import gorm.tools.security.services.AppUserService
import gorm.tools.security.services.SpringSecService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.Plugin

class GormToolsSecurityGrailsPlugin extends Plugin {

    def loadAfter = ['spring-security-core', 'spring-security-ldap', 'spring-security-rest', 'gorm-tools', 'datasource']
    def pluginExcludes = ["**/init/**"]

    Closure doWithSpring() {
        { ->
            def securityConf = SpringSecurityUtils.securityConfig
            if (securityConf.active) {
                secService(SpringSecService, AppUser)
                userService(AppUserService)

                passwordValidator(PasswordValidator)

                // spring security uses an older deprecated interface security.authentication.encoding.PasswordEncoder
                // this one wraps the new one in the old interface as spring sec's DaoAuthenticationProvider needs it
                // once thats upgraded then we can fix this
                passwordEncoder(grails.plugin.springsecurity.authentication.encoding.BCryptPasswordEncoder, 10)

                //overrrides the spring sec's userDetailsService
                userDetailsService(AppUserDetailsService)

                secLoginHandler(SecLoginHandler)
                secLogoutHandler(SecLogoutHandler)
                // authenticationDetailsSource(RallyAuthenticationDetailsSource)
            }

            //dont register beans if audit trail is disabled.
            if (config.getProperty('gorm.tools.audit.enabled', Boolean, true)) {
                //auditStampEventListener(AuditStampEventListener)
                auditStampBeforeValidateListener(AuditStampBeforeValidateListener)
                auditStampPersistenceEventListener(AuditStampPersistenceEventListener)
                auditStampSupport(AuditStampSupport)
            }


        }
    }
}
