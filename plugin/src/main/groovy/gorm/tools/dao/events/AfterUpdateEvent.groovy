package gorm.tools.dao.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired right after an bind and update. Params can be used to setup other domains associations
 */
@CompileStatic
class AfterUpdateEvent<D> extends DaoEvent<D> {

    Map params

    AfterUpdateEvent(Datastore source, D entity, Map params) {
        super(source, entity, DaoEventType.AfterUpdate.eventKey)
        this.params = params
    }

}

