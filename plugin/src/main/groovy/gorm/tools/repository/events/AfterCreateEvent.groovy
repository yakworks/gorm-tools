package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired After a bind and save/persist. Often used along with params to setup other domains associations
 *
 * @param D the entity domain class
 */
@CompileStatic
class AfterCreateEvent<D> extends RepositoryEvent<D> {

    AfterCreateEvent(Datastore source, D entity, Map data) {
        super(source, entity, RepositoryEventType.AfterCreate.eventKey)
        this.data = data
    }

}
