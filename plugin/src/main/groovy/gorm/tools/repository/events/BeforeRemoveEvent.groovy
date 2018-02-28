/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.repository.events

import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic

@CompileStatic
class BeforeRemoveEvent<D> extends RepositoryEvent<D> {

    BeforeRemoveEvent(RepositoryApi source, D entity) {
        super(source, entity, RepositoryEventType.BeforeRemove.eventKey)
    }

}
