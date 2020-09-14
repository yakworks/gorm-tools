/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.testing

import groovy.transform.CompileDynamic

import org.junit.BeforeClass
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder

import gorm.tools.audit.AuditStampBeforeValidateListener
import gorm.tools.audit.AuditStampSupport
import gorm.tools.security.domain.SecUser

/**
 * adds mock spring beans for passwordEncoder, and secService and
 * AuditStampEventListener for created/edited fields. During and entity.create it converts test data to a map
 * and since the created/edited fields are not bindable they dont get set so needs a listener
 */
@CompileDynamic
trait SecurityTest {

    //called from BuildDataTest as it setups and mocks the domains
    void onMockDomains(Class<?>... entityClasses) {
        defineBeans {
            passwordEncoder(PlaintextPasswordEncoder)
            secService(TestingSecService, SecUser)
            // auditStampEventListener(AuditStampEventListener)
            stampBeforeValidateListener(AuditStampBeforeValidateListener)
            auditStampSupport(AuditStampSupport)
        }
        // call the super which should be in one of the traits this is appended to
        super.onMockDomains(entityClasses)
    }

    // Closure doWithSpringFirst() {
    //     return {
    //         passwordEncoder(PlaintextPasswordEncoder)
    //         secService(TestingSecService, SecUser)
    //         // auditStampEventListener(AuditStampEventListener)
    //         stampBeforeValidateListener(AuditStampBeforeValidateListener)
    //         auditStampSupport(AuditStampSupport)
    //
    //         // auditStampListener(AuditStampListener)
    //         // gormToolsAuditStampListener(GormToolsAuditStampListener, getDatastore())
    //     }
    // }

    // void authenticate(SecUser user, String... roles) {
    //     roles = roles.collect { "ROLE_" + it}
    //     List authorities = AuthorityUtils.createAuthorityList(roles)
    //
    //     GrailsUser grailsUser = new GrailsUser(user.username, user.passwordHash, user.enabled, true, !user.passwordExpired, true, authorities, user.id)
    //     SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(grailsUser, user.passwordHash, authorities)
    // }

}
