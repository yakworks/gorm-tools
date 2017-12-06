package gorm.tools.dao.events

import gorm.tools.dao.DaoEntity
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.EventType

/**
 * Created by sudhir on 06/12/17.
 */
class PreDaoUpdateEvent extends AbstractPersistenceEvent {

    Map params
    DaoEntity instance

    PreDaoUpdateEvent(def instance, Map params) {
        super(null, null, null)

        this.instance = instance
        this.params = params

    }

    @Override
    EventType getEventType() {
        return EventType.PreUpdate
    }
}
