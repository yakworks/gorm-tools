/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.audit

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.PersistenceEventListener
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.grails.datastore.mapping.engine.event.ValidationEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.event.EventListener

import gorm.tools.repository.events.BeforeValidateEvent

@CompileStatic
class AuditStampListener implements PersistenceEventListener {

    @Autowired AuditStampSupport auditStampSupport

    @EventListener
    void beforeValidate(BeforeValidateEvent event){
        println "AuditStampListener beforeValidate "
    }

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
