/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.audit.scratch

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PersistenceEventListener
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEvent

import gorm.tools.audit.AuditStampSupport

@CompileStatic
class AuditStampListener implements PersistenceEventListener {

    @Autowired AuditStampSupport auditStampSupport

    void onPersistenceEvent(AbstractPersistenceEvent event){
        println "AuditStampListener onPersistenceEvent"
    }
    public final void onApplicationEvent(ApplicationEvent e) {
        if (e instanceof AbstractPersistenceEvent) {
            println "AuditStampListener onPersistenceEvent"
        }
    }

    @Override
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return PreInsertEvent.isAssignableFrom(eventType) ||
            PreUpdateEvent.isAssignableFrom(eventType)
    }

    boolean supportsSourceType(final Class<?> sourceType) {
        true
    }
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
