package gorm.tools.dao.events

import gorm.tools.dao.DaoEntity
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.EventType

class PreDaoCreateEvent extends AbstractPersistenceEvent {

    Map params
    DaoEntity instance


    protected PreDaoCreateEvent(DaoEntity instance, Map params) {
        super(null, null, null)
        this.instance = instance
        this.params = params
    }

    @Override
    EventType getEventType() {
        return EventType.PostInsert
    }
}
