package yakworks.testify

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

import gorm.tools.repository.events.AfterRemoveEvent
import gorm.tools.repository.events.BeforeBindEvent
import yakworks.rally.orgs.model.Org

// used to test spring events with RepositoryEventsSpec
@Component
class OrgRepoEventListener {

    @EventListener
    void bc(BeforeBindEvent<Org> event) {
        Org org = event.entity
        org.comments = "BeforeBindEvent ${event.bindAction}"
    }

    @EventListener
    void ar(AfterRemoveEvent<Org> event) {
        Org org = event.entity
        org.comments = "AfterRemoveEvent"
    }

    // @EventListener
    // void befPer(BeforePersistEvent event) {
    //     if(event.entity instanceof AuditStampTrait) {
    //         def ent = event.entity
    //         ent['editedBy'] = 999
    //         // ent.stampEvent = "BeforePersistEvent Stamp"
    //     }
    // }
}
