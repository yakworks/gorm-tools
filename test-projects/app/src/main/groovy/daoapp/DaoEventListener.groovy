package daoapp

import gorm.tools.dao.events.AfterRemoveEvent
import gorm.tools.dao.events.BeforeCreateEvent
import gorm.tools.dao.events.BeforeUpdateEvent
import grails.events.annotation.gorm.Listener
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
public class DaoEventListener {

    @EventListener
    void beforeCreate(BeforeCreateEvent<Org> event) {
        Org org = event.entity
        org.event = "PreDaoCreateEvent"
    }

    @EventListener
    void beforeCreate(BeforeUpdateEvent<Org> event) {
        Org org = event.entity
        org.event = "PreDaoUpdateEvent"
    }

    @EventListener
    void beforeCreate(AfterRemoveEvent<Org> event) {
        Org org = event.entity
        org.event = "PostDaoRemoveEvent"
    }
}
