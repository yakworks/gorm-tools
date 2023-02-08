/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.audit

import groovy.transform.CompileStatic

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration @Lazy
@ConditionalOnProperty(value="gorm.tools.audit.enabled", havingValue = "true", matchIfMissing = true)
@CompileStatic
class AuditStampConfiguration {

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
