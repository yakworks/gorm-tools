/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import gorm.tools.repository.api.RepositoryApi

/**
 * Fired right after a repository delete
 */
@CompileStatic
class AfterRemoveEvent<D> extends RepositoryEvent<D> {

    AfterRemoveEvent(RepositoryApi source, D entity) {
        super(source, entity, RepositoryEventType.AfterRemove.eventKey)
    }

}
