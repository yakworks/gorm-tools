package yakworks.rally.massupdate

import gorm.tools.repository.RepoLookup
import gorm.tools.repository.events.AfterMassUpdateEntityEvent
import gorm.tools.repository.events.BeforeMassUpdateEntityEvent
import groovy.transform.CompileDynamic
import org.springframework.context.event.EventListener
import spock.lang.Specification
import yakworks.api.ApiResults
import yakworks.api.problem.data.DataProblemException
import yakworks.gorm.api.massupdate.MassUpdateArgs
import yakworks.rally.activity.ActivityBulk
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

class MassUpdateServiceSpec extends Specification implements GormHibernateTest {
    static entityClasses = [KitchenSink, SinkExt, SinkItem]
    static List springBeans = [MassUpdateTestListener, RecordingActivityBulk]

    /** Prototype bean with Class ctor arg — ServiceLookup.getBean(name, [entityClass]). */
    @CompileDynamic
    Closure doWithGormBeans() {
        { ->
            defaultMassUpdateService(MassUpdateService) { bean ->
                bean.scope = 'prototype'
                bean.autowire = 'byType'
            }
        }
    }

    MassUpdateService<KitchenSink> getMassUpdateService(){
        MassUpdateService.lookup(KitchenSink)
    }

    MassUpdateTestListener getTestListener(){
        ctx.getBean(MassUpdateTestListener)
    }

    RecordingActivityBulk getActivityBulk(){
        ctx.getBean(RecordingActivityBulk)
    }

    void setup(){
        testListener.clear()
        activityBulk.clear()
    }

    /** Minimal sinks without Thing — avoids createKitchenSinks session issues. */
    List<Long> createSimpleSinks(int count) {
        (1..count).collect { int i ->
            KitchenSink.create(num: "ks$i", name: "Sink $i", kind: KitchenSink.Kind.CLIENT).id as Long
        }
    }

    void "lookup returns the service for the entity"() {
        when:
        MassUpdateService<KitchenSink> svc = MassUpdateService.lookup(KitchenSink)

        then:
        svc
        svc.entityClass == KitchenSink
        // KitchenSink.repo is a static INSTANCE that can outlive the Spring ctx in unit tests;
        // compare against RepoLookup which is what MassUpdateService uses
        svc.repo == RepoLookup.findRepo(KitchenSink)
        svc.repo.entityClass == KitchenSink
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

    void "activity is stripped from field updates and created before finished event"() {
        setup:
        List ids = createSimpleSinks(2)
        Map data = [comments: 'with-act', activity: [name: 'note']]

        when:
        ApiResults results = massUpdateService.massUpdate(MassUpdateArgs.of(ids, data))

        then:
        results.ok
        ids.every { Long id -> KitchenSink.get(id).comments == 'with-act' }
        !data.containsKey('activity')

        and:
        activityBulk.calls.size() == 1
        activityBulk.calls[0].entityClass == KitchenSink
        activityBulk.calls[0].ids == ids
        activityBulk.calls[0].activityData == [name: 'note']
        !activityBulk.calls[0].linkTargets
        testListener.finishedEvents.size() == 1
    }

    void "activity-only mass update skips field updates"() {
        when:
        ApiResults results = massUpdateService.massUpdate(MassUpdateArgs.of([10L, 20L], [activity: [name: 'only']]))

        then:
        results.ok
        results.size() == 2
        testListener.beforeEntityCount == 0
        activityBulk.calls.size() == 1
        activityBulk.calls[0].ids == [10L, 20L]
    }

    void "activity linkTargets comes from MassUpdateArgs"() {
        setup:
        MassUpdateArgs args = MassUpdateArgs.of([1L], [activity: [name: 'linked']])
        args.linkTargets = true

        when:
        massUpdateService.massUpdate(args)

        then:
        activityBulk.calls[0].linkTargets
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

/** Records createActivities calls without needing org-bearing domains. */
class RecordingActivityBulk extends ActivityBulk {
    List<Map> calls = []

    void clear() { calls.clear() }

    @Override
    void createActivities(Class entityClass, List ids, Map activityData, boolean linkTargets) {
        calls << [entityClass: entityClass, ids: ids, activityData: activityData, linkTargets: linkTargets]
    }
}
