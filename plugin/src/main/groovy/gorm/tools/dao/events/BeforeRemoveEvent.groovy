package gorm.tools.dao.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

@CompileStatic
class BeforeRemoveEvent<D> extends DaoEvent<D> {

    BeforeRemoveEvent(Datastore source, D entity) {
        super(source, entity, DaoEventType.BeforeRemove.eventKey)
    }

}
