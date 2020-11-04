/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.audit

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.EventType
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.grails.datastore.mapping.engine.event.ValidationEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEvent
import org.springframework.context.event.GenericApplicationListener
import org.springframework.core.ResolvableType

/**
 * An event listener that adds support for auto-timestamping with userId for edits and updates
 * concepts taken from the AutoTimeStampingListener in gorm. FIXME problem is on
 */
@CompileStatic
class AuditStampPersistenceEventListener implements GenericApplicationListener {
    int order = (Integer.MAX_VALUE / 2) as Integer

    @Autowired AuditStampSupport auditStampSupport

    @Override
    boolean supportsEventType(ResolvableType resolvableType) {
        Class<?> eventType = resolvableType.getRawClass()
        return PreInsertEvent.isAssignableFrom(eventType) || PreUpdateEvent.isAssignableFrom(eventType)
            //TODO see note above,
            // || ValidationEvent.isAssignableFrom(eventType)
    }

    @Override
    boolean supportsSourceType(final Class<?> sourceType) { true }

    @Override
    void onApplicationEvent(final ApplicationEvent event) {
        if(event instanceof AbstractPersistenceEvent && event.entity) {

            //if (event.eventType == EventType.PreInsert || event.eventType == EventType.Validation) {
            if (event.eventType == EventType.PreInsert) {
                if(isAuditStamped(event.entity.name)){
                    auditStampSupport.stampIfNew(event.entityObject)
                }
            }
            else if (event.eventType == EventType.PreUpdate) {
                if(isAuditStamped(event.entity.name)){
                    auditStampSupport.stampPreUpdateEvent(event.entityAccess)
                }
            }
        }
    }

    //check if the given domain class should be audit stamped.
    boolean isAuditStamped(String entityName) {
        return auditStampSupport.isAuditStamped(entityName)
    }
}
