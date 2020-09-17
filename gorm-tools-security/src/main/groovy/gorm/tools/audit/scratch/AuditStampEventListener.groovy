/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.audit.scratch

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener

import gorm.tools.audit.AuditStampSupport
import gorm.tools.repository.events.BeforeValidateEvent

/* NOT USED but shows how much cleaner it is to use @EventListener annotation
 * the problem I ran into is that you can't pass the listener into the SimpleMapDatastore that is used in unit tests
 * like its done in the SecurityTest trait, so it doesnt get picked up in those cases. Also, for some reason
 * beforeValidate was getting called 2 or 3 times in unit tests.
 * We should revisit this as its far cleaner than using the ApplicationListener concept
 */
@CompileStatic
class AuditStampEventListener {
    @Autowired AuditStampSupport auditStampSupport

    @EventListener
    void beforeValidate(BeforeValidateEvent event) {
        // println "AuditStampEventListener beforeValidate"
        if(isAuditStamped(event.entity))
            auditStampSupport.stampIfNew(event.entity)
    }

    /**
     * beforeValidate do whats needed to initialize the stamps, this is here in case save is called
     * with validation:false and it skips the beforeValidate
     */
    @EventListener
    void beforeInsert(PreInsertEvent event){
        // println "AuditStampEventListener PreInsertEvent"
        if(isAuditStamped(event.entityObject))
            auditStampSupport.stampIfNew(event.entityObject)
    }

    @EventListener
    void beforeUpdate(PreUpdateEvent event){
        // println "AuditStampEventListener PreUpdateEvent"
        if(isAuditStamped(event.entityObject)){
            auditStampSupport.stampPreUpdateEvent(event.entityAccess)
        }
    }

    //check if the given domain class should be audit stamped.
    boolean isAuditStamped(Object entity) {
        return auditStampSupport.isAuditStamped(entity.class.name)
    }

}
