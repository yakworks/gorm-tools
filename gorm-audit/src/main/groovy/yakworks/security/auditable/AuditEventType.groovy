/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.auditable

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.engine.event.EventType

/**
 * Simple enum for audit events
 */
@CompileStatic
enum AuditEventType {
    INSERT, UPDATE, DELETE

    @Override
    String toString() {
        name()
    }

    static AuditEventType forEventType(EventType type) {
        switch(type) {
            case EventType.PostInsert:
                return INSERT
            case EventType.PreDelete:
                return DELETE
            case EventType.PreUpdate:
                return UPDATE
            default:
                throw new IllegalArgumentException("Unexpected event type $type")
        }
    }
}
