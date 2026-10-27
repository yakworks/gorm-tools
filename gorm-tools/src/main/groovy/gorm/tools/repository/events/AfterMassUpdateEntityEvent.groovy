/*
* Copyright 2026 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import yakworks.gorm.api.massupdate.MassUpdateArgs

/**
 * For mass update this is fired after each individual item is updated.
 */
@CompileStatic
class AfterMassUpdateEntityEvent<D> extends RepositoryEvent<D> {

    MassUpdateArgs massUpdateArgs

    AfterMassUpdateEntityEvent(GormRepo<D> repo, D entity, Map data, MassUpdateArgs massUpdateArgs) {
        super(repo, entity, RepositoryEventType.AfterMassUpdateEntity.eventKey)
        this.data = data
        this.massUpdateArgs = massUpdateArgs
    }

}
