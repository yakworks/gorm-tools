/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy

import yakworks.rally.mail.MailSpringConfig
import yakworks.security.audit.AuditStampConfiguration
import yakworks.security.gorm.SecurityGormConfiguration
import yakworks.security.spring.DefaultSecurityConfiguration

@Configuration @Lazy
@Import([DefaultSecurityConfiguration, SecurityGormConfiguration, AuditStampConfiguration, MailSpringConfig])
@ConfigurationPropertiesScan([ "yakworks.rally" ])
@ComponentScan(['yakworks.security.gorm', 'yakworks.rally'])
@CompileStatic
class RallyConfiguration {
    //@Entity scan packages to include in additions to this Application class's package
    public static final List<String> entityScanPackages = ['yakworks.security.gorm', 'yakworks.rally']



}
