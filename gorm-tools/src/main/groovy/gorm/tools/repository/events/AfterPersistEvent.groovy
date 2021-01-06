/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import gorm.tools.repository.model.RepositoryApi

/**
 * Fired after successful repository.persist
 * may or may not have the data and bindAction set
 */
@CompileStatic
class AfterPersistEvent<D> extends RepositoryEvent<D> {

    AfterPersistEvent(RepositoryApi repo, D entity, Map args) {
        super(repo, entity, RepositoryEventType.AfterPersist.eventKey, args)
    }

}
