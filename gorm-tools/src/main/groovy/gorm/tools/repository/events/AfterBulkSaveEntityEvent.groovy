/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import gorm.tools.job.SyncJobArgs
import gorm.tools.repository.GormRepo

/**
 * For bulk processing this is fired after each individual item is created or updated.
 *
 * @param D the entity domain class
 */
@CompileStatic
class AfterBulkSaveEntityEvent<D> extends RepositoryEvent<D> {

    SyncJobArgs syncJobArgs

    AfterBulkSaveEntityEvent(GormRepo<D> repo, D entity, Map data, SyncJobArgs syncJobArgs) {
        super(repo, entity, RepositoryEventType.AfterBulkSaveEntity.eventKey)
        this.data = data
        this.syncJobArgs = syncJobArgs
    }

}
