/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing

import groovy.transform.CompileDynamic

import org.springframework.security.crypto.password.NoOpPasswordEncoder

import gorm.tools.audit.AuditStampBeforeValidateListener
import gorm.tools.audit.AuditStampPersistenceEventListener
import gorm.tools.audit.AuditStampSupport
import gorm.tools.security.domain.AppUser

/**
 * adds mock spring beans for passwordEncoder, and secService and
 * AuditStampEventListener for created/edited fields. During and entity.create it converts test data to a map
 * and since the created/edited fields are not bindable they dont get set so needs a listener
 */
@SuppressWarnings('Indentation')
@CompileDynamic
trait SecurityTest {

    //called from RepoBuildDataTest as it setups and mocks the domains
    // void onMockDomains(Class<?>... entityClasses) {
        // do these beans first so that they can get injected into the repos
        // defineBeans {
        //     passwordEncoder(NoOpPasswordEncoder)
        //     secService(TestingSecService, AppUser)
        // }
        // now mock the domains
        // super.onMockDomains(entityClasses)
        // do these after so they can cache the domains created
        // defineBeans {
        //     auditStampBeforeValidateListener(AuditStampBeforeValidateListener)
        //     auditStampPersistenceEventListener(AuditStampPersistenceEventListener)
        //     auditStampSupport(AuditStampSupport)
        // }
        // add the listener to the SimpleMapDatastore
        // datastore.applicationEventPublisher.addApplicationListener(ctx.getBean("auditStampPersistenceEventListener"))
    // }

    Closure doWithSecurity() {
        { ->
            passwordEncoder(NoOpPasswordEncoder)
            secService(TestingSecService, AppUser)
            auditStampBeforeValidateListener(AuditStampBeforeValidateListener)
            auditStampPersistenceEventListener(AuditStampPersistenceEventListener)
            auditStampSupport(AuditStampSupport)
        }
    }

    void doAfterDomains() {
        datastore.applicationEventPublisher.addApplicationListener(ctx.getBean("auditStampPersistenceEventListener"))
    }

}
