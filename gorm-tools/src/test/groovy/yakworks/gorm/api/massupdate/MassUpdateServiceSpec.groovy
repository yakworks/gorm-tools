package yakworks.gorm.api.massupdate

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.api.ApiResults
import yakworks.api.problem.data.DataProblemException
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.unit.GormHibernateTest

class MassUpdateServiceSpec extends Specification implements GormHibernateTest {
    static entityClasses = [KitchenSink, SinkExt]

    @Autowired MassUpdateService massUpdateService

    void "mass update applies shared data to all ids"() {
        given:
        KitchenSink.createKitchenSinks(3)
        List ids = KitchenSink.list()*.id

        when:
        ApiResults results = massUpdateService.massUpdate(KitchenSink, MassUpdateArgs.of(ids, [comments: 'mass-updated']))

        then:
        results.ok
        results.size() == 3
        KitchenSink.list().every { it.comments == 'mass-updated' }
    }

    void "mass update with few failures"() {
        given:
        KitchenSink.createKitchenSinks(2)
        List ids = KitchenSink.list()*.id + [99999L]

        when:
        ApiResults results = massUpdateService.massUpdate(KitchenSink, MassUpdateArgs.of(ids, [comments: 'partial']))

        then:
        !results.ok
        results.size() == 3
        results.list.count { it.ok } == 2
        results.list.count { !it.ok } == 1
    }

    void "empty ids or data throws"() {
        when:
        massUpdateService.massUpdate(KitchenSink, MassUpdateArgs.of([], [name2: 'x']))

        then:
        DataProblemException ex = thrown()
        ex.code == 'error.data.emptyPayload'

        when:
        massUpdateService.massUpdate(KitchenSink, MassUpdateArgs.of([1L], [:]))

        then:
        DataProblemException ex2 = thrown()
        ex2.code == 'error.data.emptyPayload'
    }
}
