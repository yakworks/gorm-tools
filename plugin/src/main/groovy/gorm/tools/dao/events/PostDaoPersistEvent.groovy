package gorm.tools.dao.events

import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.EntityAccess
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.EventType
import org.grails.datastore.mapping.model.PersistentEntity

/**
 * Created by sudhir on 06/12/17.
 */
class PostDaoPersistEvent extends AbstractPersistenceEvent {

    PostDaoPersistEvent(Datastore source, Object entity) {
        super(source, entity)
    }

    @Override
    EventType getEventType() {
        return EventType.SaveOrUpdate
    }
}
