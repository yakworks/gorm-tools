/*
* Copyright 2026 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import yakworks.gorm.api.massupdate.MassUpdateArgs

/**
 * For mass update this is fired before each individual item is updated.
 * Gives a chance to modify the data before it gets bound.
 *
 * @param D the entity domain class
 */
@CompileStatic
class BeforeMassUpdateEntityEvent<D> extends RepositoryEvent<D> {

    MassUpdateArgs massUpdateArgs

    BeforeMassUpdateEntityEvent(GormRepo<D> repo, Map data, MassUpdateArgs massUpdateArgs) {
        super(repo, RepositoryEventType.BeforeMassUpdateEntity.eventKey)
        this.data = data
        this.massUpdateArgs = massUpdateArgs
    }

}
