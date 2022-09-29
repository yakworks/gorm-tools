/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.unit

import groovy.transform.CompileDynamic

import org.springframework.security.crypto.password.NoOpPasswordEncoder

import yakworks.security.audit.AuditStampBeforeValidateListener
import yakworks.security.audit.AuditStampPersistenceEventListener
import yakworks.security.audit.AuditStampSupport
import yakworks.security.audit.DefaultAuditUserResolver
import yakworks.security.user.CurrentUserHolder
import yakworks.testing.gorm.CurrentTestUser
import yakworks.testing.gorm.TestingSecService

/**
 * Supports the gorm-security plugin.
 * adds mock spring beans for passwordEncoder, and secService and
 * AuditStampEventListener for created/edited fields. During and entity.create it converts test data to a map
 * and since the created/edited fields are not bindable they dont get set so needs a listener
 */
@SuppressWarnings('Indentation')
@CompileDynamic
trait SecurityTest {

    Closure doWithSecurityBeans() {
        { ->
            passwordEncoder(NoOpPasswordEncoder)
            currentUserHolder(CurrentUserHolder)
            currentUser(CurrentTestUser)
            secService(TestingSecService)
            auditUserResolver(DefaultAuditUserResolver)
            auditStampBeforeValidateListener(AuditStampBeforeValidateListener)
            auditStampPersistenceEventListener(AuditStampPersistenceEventListener)
            auditStampSupport(AuditStampSupport)
        }
    }

    void doAfterGormBeans() {
        datastore.applicationEventPublisher.addApplicationListener(ctx.getBean("auditStampPersistenceEventListener"))
    }

}
