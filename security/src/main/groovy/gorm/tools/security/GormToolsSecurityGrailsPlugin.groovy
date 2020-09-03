/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import gorm.AuditStampConfigLoader
import gorm.FieldProps
import grails.plugin.springsecurity.SpringSecurityUtils
import gorm.tools.security.stamp.AuditStampEventListener
import grails.plugins.Plugin

class GormToolsSecurityGrailsPlugin extends Plugin {
    def grailsVersion = "3.3.10 > *"

    def title = "Rally Security"
    def author = "Your name"
    def authorEmail = ""
    def description = "Brief summary/description of the plugin."

    def documentation = "http://grails.org/plugin/rally-security"

    def loadAfter = ['spring-security-core', 'spring-security-ldap', 'spring-security-rest', 'gorm-tools', 'datasource']
    def pluginExcludes = ["**/init/**"]

    Closure doWithSpring() {
        { ->
            def securityConf = SpringSecurityUtils.securityConfig
            if (securityConf.active) {
                passwordValidator(PasswordValidator){ bean ->
                    bean.autowire = "byName"
                }
                passwordEncoder(grails.plugin.springsecurity.authentication.encoding.BCryptPasswordEncoder, 10)

                userDetailsService(RallyUserDetailsService)

                nineLoginHandler(RallyLoginHandler) { bean ->
                    bean.autowire = "byName"
                }
                rallyLogoutHandler(RallyLogoutHandler)
                authenticationDetailsSource(RallyAuthenticationDetailsSource)
            }

            //dont register beans if audit trail is disabled.
            if (grailsApplication.config.grails.plugin.audittrail.enabled ){
                Map fprops = FieldProps.buildFieldMap(new AuditStampConfigLoader().load())

                auditStampEventListener(AuditStampEventListener, ref('hibernateDatastore')) {
                    grailsApplication = grailsApplication
                    springSecurityService = ref("springSecurityService")
                    fieldProps = fprops
                }
            }


        }
    }
}
