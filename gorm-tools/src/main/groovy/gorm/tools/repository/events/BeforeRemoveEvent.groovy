/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import gorm.tools.repository.model.RepositoryApi

@CompileStatic
class BeforeRemoveEvent<D> extends RepositoryEvent<D> {

    BeforeRemoveEvent(RepositoryApi source, D entity, Map args) {
        super(source, entity, RepositoryEventType.BeforeRemove.eventKey, args)
    }

}
