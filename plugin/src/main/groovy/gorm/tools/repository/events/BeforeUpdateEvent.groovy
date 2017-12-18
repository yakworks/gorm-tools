package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired right before an update
 */
@CompileStatic
class BeforeUpdateEvent<D> extends RepositoryEvent<D> {

    Map params

    BeforeUpdateEvent(Datastore source, D entity, Map params) {
        super(source, entity, RepositoryEventType.BeforeUpdate.eventKey)
        this.params = params
    }

}
