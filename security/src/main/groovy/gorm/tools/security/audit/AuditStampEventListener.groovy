/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.audit


import groovy.transform.CompileStatic

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener

import gorm.tools.repository.events.BeforeValidateEvent

@CompileStatic
class AuditStampEventListener {
    @Autowired AuditStampSupport auditStampSupport

    @EventListener
    void beforeValidate(BeforeValidateEvent event){
        println "AuditStampEventListener beforeValidate"
        if(isAuditStamped(event.entity))
            auditStampSupport.stampIfNew(event.entity)
    }

    @EventListener
    void beforeInsert(PreInsertEvent event){
        if(isAuditStamped(event.entityObject))
            auditStampSupport.stampIfNew(event.entityObject)
    }

    @EventListener
    void beforeUpdate(PreUpdateEvent event){
        if(isAuditStamped(event.entityObject)){
            auditStampSupport.stampPreUpdateEvent(event)
        }
    }

    //check if the given domain class should be audit stamped.
    boolean isAuditStamped(Object entity) {
        return auditStampSupport.isAuditStamped(entity.class.name)
    }

}
