package yakworks.testify

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

import gorm.tools.job.SyncJobFinishedEvent
import gorm.tools.job.SyncJobStartEvent
import gorm.tools.repository.model.DataOp
import yakworks.api.OkResult
import yakworks.rally.orgs.model.Org

/**
 * Used for testing sync job events
 */
@Component
class SyncjobEventListener {

    @EventListener
    void onBulk(SyncJobFinishedEvent<Org> event) {
        if(event.context.args.op != DataOp.add) return
        assert event.domainClass.isAssignableFrom(Org)
        event.context.results.each {
            if (it instanceof OkResult) {
                Long id = it.payload.id as Long
                if (id != null) {
                    Org org = Org.get(id)
                    org.comments = "${org.num}-SyncjobEventListener"
                    org.save(flush: true)
                }
            }
        }
    }

    @EventListener
    void beforeBulk(SyncJobStartEvent<Org> event) {
        if(event.context.args.op != DataOp.add) return
        if(event.context.payload && event.context.payload instanceof Collection) {
            event.context.payload.each {
                if(it['info']) {
                    it['info'].fax = "SyncjobEventListener"
                }
            }
        }
    }
}
