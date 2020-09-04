/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import gorm.tools.compiler.stamp.AuditStampConfigLoader
import gorm.tools.compiler.stamp.FieldProps
import gorm.tools.security.stamp.GormToolsAuditStampListener
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.Plugin

class GormToolsSecurityGrailsPlugin extends Plugin {

    def loadAfter = ['spring-security-core', 'spring-security-ldap', 'spring-security-rest', 'gorm-tools', 'datasource']
    def pluginExcludes = ["**/init/**"]

    Closure doWithSpring() {
        { ->
            def securityConf = SpringSecurityUtils.securityConfig
            if (securityConf.active) {
                passwordValidator(PasswordValidator)

                passwordEncoder(grails.plugin.springsecurity.authentication.encoding.BCryptPasswordEncoder, 10)
                //overrrides the spring sec's userDetailsService
                userDetailsService(SecUserDetailsService)

                secLoginHandler(SecLoginHandler)
                secLogoutHandler(SecLogoutHandler)
                // authenticationDetailsSource(RallyAuthenticationDetailsSource)
            }

            //dont register beans if audit trail is disabled.
            if (config.getProperty('grails.plugin.audittrail.enabled', Boolean, true)) {
                Map fprops = FieldProps.buildFieldMap(new AuditStampConfigLoader().load())

                gormToolsAuditStampListener(GormToolsAuditStampListener, ref('hibernateDatastore')) { bean ->
                    bean.lazyInit = true
                    fieldProps = fprops
                }
            }


        }
    }
}
