/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import gorm.tools.security.audit.GormToolsAuditStampListener
import gorm.tools.security.audit.ast.AuditStampConfigLoader
import gorm.tools.security.audit.ast.FieldProps
import gorm.tools.security.domain.SecUser
import gorm.tools.security.services.SpringSecService
import gorm.tools.security.services.UserService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.Plugin

class GormToolsSecurityGrailsPlugin extends Plugin {

    def loadAfter = ['spring-security-core', 'spring-security-ldap', 'spring-security-rest', 'gorm-tools', 'datasource']
    def pluginExcludes = ["**/init/**"]

    Closure doWithSpring() {
        { ->
            def securityConf = SpringSecurityUtils.securityConfig
            if (securityConf.active) {
                secService(SpringSecService, SecUser)
                userService(UserService)

                passwordValidator(PasswordValidator)

                // spring security uses an older deprecated interface security.authentication.encoding.PasswordEncoder
                // this one wraps the new one in the old interface as spring sec's DaoAuthenticationProvider needs it
                // once thats upgraded then we can fix this
                passwordEncoder(grails.plugin.springsecurity.authentication.encoding.BCryptPasswordEncoder, 10)

                //overrrides the spring sec's userDetailsService
                userDetailsService(SecUserDetailsService)

                secLoginHandler(SecLoginHandler)
                secLogoutHandler(SecLogoutHandler)
                // authenticationDetailsSource(RallyAuthenticationDetailsSource)
            }

            //dont register beans if audit trail is disabled.
            if (config.getProperty('gorm.tools.security.audit.enabled', Boolean, true)) {
                Map fprops = FieldProps.buildFieldMap(new AuditStampConfigLoader().load())

                gormToolsAuditStampListener(GormToolsAuditStampListener, ref('hibernateDatastore')) { bean ->
                    bean.lazyInit = true
                    fieldProps = fprops
                }
            }


        }
    }
}
