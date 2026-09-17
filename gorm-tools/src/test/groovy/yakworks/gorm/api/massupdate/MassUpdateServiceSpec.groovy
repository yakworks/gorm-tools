package yakworks.gorm.api.massupdate

import gorm.tools.repository.events.AfterMassUpdateEntityEvent
import gorm.tools.repository.events.BeforeMassUpdateEntityEvent
import org.springframework.context.event.EventListener
import spock.lang.Specification
import yakworks.api.ApiResults
import yakworks.api.problem.data.DataProblemException
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

class MassUpdateServiceSpec extends Specification implements GormHibernateTest {
    static entityClasses = [KitchenSink, SinkExt, SinkItem]
    static List springBeans = [MassUpdateTestListener]

    MassUpdateService<KitchenSink> getMassUpdateService(){
        MassUpdateService.lookup(KitchenSink)
    }

    MassUpdateTestListener getTestListener(){
        ctx.getBean(MassUpdateTestListener)
    }

    void setup(){
        testListener.clear()
    }

    void "lookup returns the service for the entity"() {
        when:
        MassUpdateService<KitchenSink> svc = MassUpdateService.lookup(KitchenSink)

        then:
        svc
        svc.entityClass == KitchenSink
        svc.repo == KitchenSink.repo
    }

    void "mass update applies shared data to all ids"() {
        setup:
        KitchenSink.createKitchenSinks(3)
        List ids = KitchenSink.list()*.id

        when:
        ApiResults results = massUpdateService.massUpdate(MassUpdateArgs.of(ids, [comments: 'mass-updated']))

        then:
        results.ok
        results.size() == 3
        KitchenSink.list().every { it.comments == 'mass-updated' }
    }

    void "mass update with few failures"() {
        setup:
        KitchenSink.createKitchenSinks(3)
        List ids = KitchenSink.list()*.id + [99999L]

        when:
        ApiResults results = massUpdateService.massUpdate(MassUpdateArgs.of(ids, [comments: 'partial']))

        then:
        !results.ok
        results.size() == 4
        results.list.count { it.ok } == 3
        results.list.count { !it.ok } == 1
    }

    void "fires the entity events and the finished event"() {
        setup:
        KitchenSink.createKitchenSinks(3)
        List ids = KitchenSink.list()*.id

        when:
        ApiResults results = massUpdateService.massUpdate(MassUpdateArgs.of(ids, [comments: 'events']))

        then: "entity events fired for each id"
        results.ok
        testListener.beforeEntityCount == 3
        testListener.afterEntityCount == 3

        and: "finished event fired once with the results"
        testListener.finishedEvents.size() == 1
        testListener.finishedEvents[0].entityClass == KitchenSink
        testListener.finishedEvents[0].ok
        testListener.finishedEvents[0].results.size() == 3
    }

    void "empty ids or data throws"() {
        when:
        massUpdateService.massUpdate(MassUpdateArgs.of([], [comments: 'x']))

        then:
        DataProblemException ex = thrown()
        ex.code == 'error.data.emptyPayload'

        when:
        massUpdateService.massUpdate(MassUpdateArgs.of([1L], [:]))

        then:
        DataProblemException ex2 = thrown()
        ex2.code == 'error.data.emptyPayload'
    }
}

class MassUpdateTestListener {
    int beforeEntityCount = 0
    int afterEntityCount = 0
    List<MassUpdateFinishedEvent> finishedEvents = []

    void clear(){
        beforeEntityCount = 0
        afterEntityCount = 0
        finishedEvents = []
    }

    @EventListener
    void beforeMassUpdateEntity(BeforeMassUpdateEntityEvent<KitchenSink> e) {
        assert e.data.id
        assert e.massUpdateArgs
        beforeEntityCount++
    }

    @EventListener
    void afterMassUpdateEntity(AfterMassUpdateEntityEvent<KitchenSink> e) {
        assert e.entity
        afterEntityCount++
    }

    @EventListener
    void massUpdateFinished(MassUpdateFinishedEvent<KitchenSink> e) {
        finishedEvents << e
    }
}
