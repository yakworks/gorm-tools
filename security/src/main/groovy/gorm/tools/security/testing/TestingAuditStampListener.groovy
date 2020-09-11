/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.testing

import java.time.LocalDateTime

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationListener

import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.security.audit.AuditStampTrait

@CompileStatic
class TestingAuditStampListener implements ApplicationListener<BeforePersistEvent> {

    @Override
    void onApplicationEvent(BeforePersistEvent event) {
        def ent = event.entity
        println "BeforePersistEvent ent $ent"
        if(ent instanceof AuditStampTrait){
            ent['createdBy'] = 1
            ent['editedBy'] = 1
            ent['editedDate'] = LocalDateTime.now()
            ent['createdDate'] = LocalDateTime.now()
        }
        // println "TestingAuditStampListener event ${event.eventKey}"
    }

}
