/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally

import groovy.transform.CompileStatic

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy

import yakworks.security.audit.AuditStampConfiguration
import yakworks.security.gorm.SecurityGormConfiguration
import yakworks.security.spring.SpringSecurityConfiguration

@Configuration @Lazy
@Import([SpringSecurityConfiguration, SecurityGormConfiguration, AuditStampConfiguration])
//@ComponentScan(['yakworks.rally']) //scan and pick up all
@ComponentScan(['yakworks.security.gorm', 'yakworks.rally'])
@CompileStatic
class RallyConfiguration {
    //@Entity scan packages to include in additions to this Application class's package
    public static final List<String> entityScanPackages = ['yakworks.security.gorm', 'yakworks.rally']
}
