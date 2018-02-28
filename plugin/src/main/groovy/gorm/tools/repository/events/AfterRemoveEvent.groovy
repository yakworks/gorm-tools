/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.repository.events

import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic

/**
 * Fired right after a repository delete
 */
@CompileStatic
class AfterRemoveEvent<D> extends RepositoryEvent<D> {

    AfterRemoveEvent(RepositoryApi source, D entity) {
        super(source, entity, RepositoryEventType.AfterRemove.eventKey)
    }

}
