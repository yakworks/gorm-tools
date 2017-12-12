package gorm.tools.dao.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired after successful dao.persist
 */
@CompileStatic
class AfterPersistEvent<D> extends DaoEvent<D>{

    AfterPersistEvent(Datastore source, D entity) {
        super(source, entity, DaoEventType.AfterPersist.eventKey)
    }

}
