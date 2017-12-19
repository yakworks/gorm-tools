package repoapp

import gorm.tools.repository.events.AfterRemoveEvent
import gorm.tools.repository.events.BeforeBindEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
public class RepoEventListener {

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
