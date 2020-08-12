package yakworks.taskify

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

import gorm.tools.repository.events.AfterRemoveEvent
import gorm.tools.repository.events.BeforeBindEvent
import yakworks.taskify.domain.Org

@Component
public class OrgRepoEventListener {

    @EventListener
    void bc(BeforeBindEvent<Org> event) {
        Org org = event.entity
        org.event = "BeforeBindEvent ${event.bindAction}"
    }

    @EventListener
    void ar(AfterRemoveEvent<Org> event) {
        Org org = event.entity
        org.event = "AfterRemoveEvent"
    }
}
