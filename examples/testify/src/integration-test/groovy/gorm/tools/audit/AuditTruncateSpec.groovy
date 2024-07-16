/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package gorm.tools.audit

import grails.core.GrailsApplication
import test.Tunnel
import yakworks.rally.audit.AuditTrail
import yakworks.security.auditable.AuditLogListener
import yakworks.security.auditable.AuditLoggingConfigUtils
import grails.testing.mixin.integration.Integration
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Slf4j
class AuditTruncateSpec extends Specification {
    @Autowired
    GrailsApplication grailsApplication

    @Shared
    def defaultIgnoreList

    void setup() {
        defaultIgnoreList = ['id'] + AuditLoggingConfigUtils.auditConfig.excluded?.asImmutable() ?: []
        AuditTrail.withNewTransaction {
            AuditTrail.executeUpdate('delete from AuditTrail')
        }
    }

    void "No_Truncate"() {
        given:
        def oldTruncLength = getFirstListenerTruncateLength()
        setListenersTruncateLength(255)

        when:
        Tunnel.withNewTransaction {
            new Tunnel(name: "shortdesc", description: "${'a' * 255}").save(flush: true, failOnError: true)
        }

        then: "description is not truncated from auditLog"
        def events = AuditTrail.withNewTransaction {
            AuditTrail.withCriteria { eq('className', 'test.Tunnel') }
        }
        events.size() == (Tunnel.gormPersistentEntity.persistentPropertyNames - defaultIgnoreList).size()

        def first = events.find { it.propertyName == 'name' }
        first.oldValue == null
        first.newValue == "shortdesc"

        def second = events.find { it.propertyName == 'description' }
        second.oldValue == null
        second.newValue == 'a' * 255

        cleanup:
        log.info "Reset truncate length"
        setListenersTruncateLength oldTruncLength
    }

    void "Truncate_at_255"() {
        given:
        def oldTruncLength = getFirstListenerTruncateLength()
        setListenersTruncateLength(255)
        Tunnel.withNewTransaction {
            new Tunnel(name: "shortdesc", description: "${'b' * 1024}").save(flush: true, failOnError: true)
        }

        expect: "description is truncated at 255 from auditLog"
        def events = AuditTrail.withNewTransaction {
            AuditTrail.withCriteria { eq('className', 'test.Tunnel') }
        }
        events.size() == (Tunnel.gormPersistentEntity.persistentPropertyNames - defaultIgnoreList).size()

        def first = events.find { it.propertyName == 'name' }
        first.oldValue == null
        first.newValue == "shortdesc"

        def second = events.find { it.propertyName == 'description' }
        log.info second.newValue
        second.oldValue == null
        second.newValue == 'b' * 255

        cleanup:
        setListenersTruncateLength oldTruncLength
    }

    void "Truncate_at_1024"() {
        given:
        def oldTruncLength = getFirstListenerTruncateLength()
        setListenersTruncateLength(1024)
        Tunnel.withNewTransaction {
            new Tunnel(name: "shortdesc", description: "${'b' * 4096}").save(flush: true, failOnError: true)
        }

        expect: "description is truncated at 255 from auditLog"
        def events = AuditTrail.withNewTransaction {
            AuditTrail.withCriteria { eq('className', 'test.Tunnel') }
        }
        events.size() == (Tunnel.gormPersistentEntity.persistentPropertyNames - defaultIgnoreList).size()

        def first = events.find { it.propertyName == 'name' }
        first.oldValue == null
        first.newValue == "shortdesc"

        def second = events.find { it.propertyName == 'description' }
        log.debug second.newValue
        second.oldValue == null
        second.newValue == 'b' * 1024

        cleanup:
        log.debug "Reset truncate length"
        setListenersTruncateLength oldTruncLength
    }

    private int getFirstListenerTruncateLength() {
        List<AuditLogListener> auditListeners = grailsApplication.parentContext.applicationEventMulticaster.applicationListeners.findAll { it.class.simpleName == "AuditLogListener" }
        auditListeners?.first()?.truncateLength
    }

    private void setListenersTruncateLength(int truncateLength) {
        log.info("Set all AuditLogListeners truncate length ${truncateLength}")
        // Change the truncateLength of all auditLogListeners - Never do that in your application!
        List<AuditLogListener> auditListeners = grailsApplication.parentContext.applicationEventMulticaster.applicationListeners.findAll { it.class.simpleName == "AuditLogListener" }
        auditListeners*.truncateLength = truncateLength
    }
}
