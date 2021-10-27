/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing

import java.time.LocalDateTime

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationListener

import gorm.tools.audit.AuditStampTrait
import gorm.tools.repository.events.BeforePersistEvent

@CompileStatic
class TestingAuditStampListener implements ApplicationListener<BeforePersistEvent> {

    @Override
    void onApplicationEvent(BeforePersistEvent event) {
        def ent = event.entity
        if(ent instanceof AuditStampTrait){
            if(!ent['createdBy']) ent['createdBy'] = 1
            if(!ent['editedBy']) ent['editedBy'] = 1
            if(!ent['editedDate']) ent['editedDate'] = LocalDateTime.now()
            if(!ent['createdDate']) ent['createdDate'] = LocalDateTime.now()
        }
        // println "TestingAuditStampListener event ${event.eventKey}"
    }

}
