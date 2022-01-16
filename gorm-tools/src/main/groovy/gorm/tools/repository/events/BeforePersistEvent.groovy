/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs

/**
 * Fired right before enitity save inside repository.persist
 */
@CompileStatic
class BeforePersistEvent<D> extends RepositoryEvent<D> {

    BeforePersistEvent(GormRepo<D> source, D entity, PersistArgs args) {
        super(source, entity, RepositoryEventType.BeforePersist.eventKey, args)
    }

}
