package yakworks.testify

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

import gorm.tools.job.SyncJobFinishedEvent
import gorm.tools.job.SyncJobStartEvent
import gorm.tools.repository.events.AfterBulkSaveEntityEvent
import gorm.tools.repository.events.BeforeBulkSaveEntityEvent
import gorm.tools.repository.model.DataOp
import yakworks.api.OkResult
import yakworks.rally.orgs.model.Org

/**
 * Used for testing sync job events
 */
@Component
class SyncjobEventListener {

    @EventListener
    void beforeBulkSaveEntity(BeforeBulkSaveEntityEvent<Org> e) {
        assert e.entityClass.isAssignableFrom(Org) //verify, tht listener is called for only org events based on generic
        e.data['flex']['text9'] = 'from before'
    }

    @EventListener
    void afterBulkSaveEntity(AfterBulkSaveEntityEvent<Org> e) {
        assert e.entityClass.isAssignableFrom(Org)
        e.entity.flex.text10 = 'from after'
    }

    @EventListener
    void onBulk(SyncJobFinishedEvent<Org> event) {
        assert event.entityClass.isAssignableFrom(Org) //verify, tht listener is called for only org events based on generic
        if(event.context.args.op != DataOp.add) return
        event.context.results.list.each {
            if (it instanceof OkResult) {
                Long id = it.payload.id as Long
                if (id != null) {
                    Org org = Org.get(id)
                    org.comments = "${org.num}-SyncJobFinishedEvent"
                    org.save(flush: true)
                }
            }
        }
    }

    @EventListener
    void beforeBulk(SyncJobStartEvent<Org> event) {
        assert event.entityClass.isAssignableFrom(Org)
        if(event.context.args.op != DataOp.add) return
        if(event.context.payload && event.context.payload instanceof Collection) {
            event.context.payload.each {
                if(it['info']) {
                    it['info'].fax = "SyncJobStartEvent"
                }
            }
        }
    }

    @EventListener
    void withoutEntityClassStart(SyncJobStartEvent<Object> event) {
        if(event.entityClass != Object) return //apply just for the event without entityClass
        if(event.context.payload && event.context.payload instanceof Collection) {
           event.context.payload << 5 //add a new item to payload which can be verified by test
        }
    }

    @EventListener
    void withoutEntityClassfinish(SyncJobFinishedEvent<Object> event) {
        if(event.entityClass != Object) return
        if(event.context.payload && event.context.payload instanceof Collection) {
            event.context.payload << 6
        }
    }
}
