package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired right after an bind and update. Params can be used to setup other domains associations
 */
@CompileStatic
class AfterUpdateEvent<D> extends RepositoryEvent<D> {

    Map params

    AfterUpdateEvent(Datastore source, D entity, Map params) {
        super(source, entity, RepositoryEventType.AfterUpdate.eventKey)
        this.params = params
    }

}

