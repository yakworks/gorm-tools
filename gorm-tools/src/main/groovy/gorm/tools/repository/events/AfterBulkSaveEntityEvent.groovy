/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import yakworks.gorm.api.bulk.BulkImportJobArgs

/**
 * For bulk processing this is fired after each individual item is created or updated.
 *
 * @param D the entity domain class
 */
@CompileStatic
class AfterBulkSaveEntityEvent<D> extends RepositoryEvent<D> {

    BulkImportJobArgs syncJobArgs

    AfterBulkSaveEntityEvent(GormRepo<D> repo, D entity, Map data, BulkImportJobArgs syncJobArgs) {
        super(repo, entity, RepositoryEventType.AfterBulkSaveEntity.eventKey)
        this.data = data
        this.syncJobArgs = syncJobArgs
    }

}
