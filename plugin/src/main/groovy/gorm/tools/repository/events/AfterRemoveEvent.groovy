package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired right after a repository delete
 */
@CompileStatic
class AfterRemoveEvent<D> extends RepositoryEvent<D> {

    AfterRemoveEvent(Datastore source, D entity) {
        super(source, entity, RepositoryEventType.AfterRemove.eventKey)
    }

}
