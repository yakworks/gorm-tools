package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired after successful repository.persist
 */
@CompileStatic
class AfterPersistEvent<D> extends RepositoryEvent<D> {

    AfterPersistEvent(Datastore source, D entity) {
        super(source, entity, RepositoryEventType.AfterPersist.eventKey)
    }

}
