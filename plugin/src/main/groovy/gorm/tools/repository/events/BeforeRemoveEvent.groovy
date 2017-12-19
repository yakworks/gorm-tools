package gorm.tools.repository.events

import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic

@CompileStatic
class BeforeRemoveEvent<D> extends RepositoryEvent<D> {

    BeforeRemoveEvent(RepositoryApi source, D entity) {
        super(source, entity, RepositoryEventType.BeforeRemove.eventKey)
    }

}
