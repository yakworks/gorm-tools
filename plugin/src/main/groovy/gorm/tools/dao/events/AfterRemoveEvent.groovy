package gorm.tools.dao.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired right after a dao delete
 */
@CompileStatic
class AfterRemoveEvent<D> extends DaoEvent<D> {

    AfterRemoveEvent(Datastore source, D entity) {
        super(source, entity, DaoEventType.AfterRemove.eventKey)
    }

}
