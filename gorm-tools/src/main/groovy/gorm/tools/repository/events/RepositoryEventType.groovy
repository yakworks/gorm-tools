/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import yakworks.commons.lang.NameUtils

@CompileStatic
enum RepositoryEventType {

    BeforeRemove(BeforeRemoveEvent),
    BeforeBind(BeforeBindEvent),
    BeforePersist(BeforePersistEvent),
    BeforeValidate(BeforeValidateEvent),
    AfterRemove(AfterRemoveEvent),
    AfterBind(AfterBindEvent),
    AfterPersist(AfterPersistEvent),

    BeforeBulkSaveEntity(BeforeBulkSaveEntityEvent),
    AfterBulkSaveEntity(AfterBulkSaveEntityEvent)

    Class eventClass
    String eventKey

    RepositoryEventType(Class clazz) {
        this.eventClass = clazz
        this.eventKey = NameUtils.getPropertyName(name())
    }
}
