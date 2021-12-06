/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.csv


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

import gorm.tools.audit.AuditStampBeforeValidateListener
import gorm.tools.audit.AuditStampPersistenceEventListener
import gorm.tools.audit.AuditStampSupport
import gorm.tools.security.domain.AppUser
import gorm.tools.security.services.AppUserService
import gorm.tools.security.services.SpringSecService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.Plugin

@SuppressWarnings(['Indentation'])
class GormToolsSecurityGrailsPlugin extends Plugin {

    def loadAfter = ['gorm-tools']
    def pluginExcludes = ["**/init/**"]

    Closure doWithSpring() { { ->


    } }

}
