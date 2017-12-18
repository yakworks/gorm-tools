package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

@CompileStatic
class BeforeRemoveEvent<D> extends RepositoryEvent<D> {

    BeforeRemoveEvent(Datastore source, D entity) {
        super(source, entity, RepositoryEventType.BeforeRemove.eventKey)
    }

}
