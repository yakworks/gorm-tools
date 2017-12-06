package gorm.tools.dao.events

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.EventType

/**
 * Created by sudhir on 06/12/17.
 */
class PreDaoPersistEvent extends AbstractPersistenceEvent {

    protected PreDaoPersistEvent() {
        super(null, null, null)
    }

    @Override
    EventType getEventType() {
        return EventType.PreInsert
    }
}
