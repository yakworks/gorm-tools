package daoapp

import gorm.tools.dao.events.PostDaoRemoveEvent
import gorm.tools.dao.events.PreDaoCreateEvent
import gorm.tools.dao.events.PreDaoUpdateEvent
import grails.events.annotation.gorm.Listener
import org.springframework.stereotype.Component

@Component
public class DaoEventListener {

    @Listener(Org)
    void beforeCreate(PreDaoCreateEvent event) {
        Org org = event.entityObject
        org.event = "PreDaoCreateEvent"
    }

    @Listener(Org)
    void beforeCreate(PreDaoUpdateEvent event) {
        Org org = event.entityObject
        org.event = "PreDaoUpdateEvent"
    }

    @Listener(Org)
    void beforeCreate(PostDaoRemoveEvent event) {
        Org org = event.entityObject
        org.event = "PostDaoRemoveEvent"
    }
}
