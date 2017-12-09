package gorm.tools.dao.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired right before enitity save inside dao.persist
 */
@CompileStatic
class BeforePersistEvent<D> extends DaoEvent<D> {

    BeforePersistEvent(Datastore source, D entity) {
        super(source, entity, DaoEventType.BeforePersist.eventKey)
    }

}
