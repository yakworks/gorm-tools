package gorm.tools.dao.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired right before an update
 */
@CompileStatic
class BeforeUpdateEvent<D> extends DaoEvent<D> {

    Map params

    BeforeUpdateEvent(Datastore source, D entity, Map params) {
        super(source, entity, DaoEventType.BeforeUpdate.eventKey)
        this.params = params
    }

}
