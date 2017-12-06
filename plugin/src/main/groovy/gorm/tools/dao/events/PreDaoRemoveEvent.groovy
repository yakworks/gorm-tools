package gorm.tools.dao.events

import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.EventType


class PreDaoRemoveEvent extends AbstractPersistenceEvent {

    PreDaoRemoveEvent(Datastore source, Object entity) {
        super(source, entity)
    }

    @Override
    EventType getEventType() {
        return EventType.Validation
    }
}
