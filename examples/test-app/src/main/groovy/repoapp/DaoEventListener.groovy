package repoapp

import gorm.tools.repository.events.AfterRemoveEvent
import gorm.tools.repository.events.BeforeCreateEvent
import gorm.tools.repository.events.BeforeUpdateEvent
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
