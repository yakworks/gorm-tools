package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired before a bind and save/persist. Allows modification to params used in the binding
 *
 * @param D the entity domain class
 */
@CompileStatic
class BeforeCreateEvent<D> extends RepositoryEvent<D> {

    BeforeCreateEvent(Datastore source, D entity, Map data) {
        super(source, entity, RepositoryEventType.BeforeCreate.eventKey)
        this.data = data
    }

}
