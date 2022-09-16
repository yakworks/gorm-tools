package yakworks.testing.gorm.model

import gorm.tools.async.AsyncService
import gorm.tools.utils.BenchmarkHelper
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.testing.gorm.unit.GormHibernateTest

class KitchenSinkRepoSpec extends Specification implements GormHibernateTest  {
    static entityClasses = [KitchenSink, SinkItem, SinkExt]
    static int SINK_COUNT = 5000

    @Autowired AsyncService asyncService
    @Autowired KitchenSinkRepo kitchenSinkRepo

    // @Transactional
    void setupSpec() {
        BenchmarkHelper.startTime()
        KitchenSink.createKitchenSinks(SINK_COUNT)
        BenchmarkHelper.printEndTimeMsg("KitchenSeedData.createKitchenSinks")
    }

    // @Transactional
    void cleanupSpec() {
        KitchenSink.truncate()
    }

    void "sanity check"() {
        expect:
        SINK_COUNT == KitchenSink.count()
        SINK_COUNT == KitchenSink.list().size()
    }

    void "sanity check fields"() {
        when:
        def ks = KitchenSink.get(1)

        then:
        2 == ks.items.size()
        ks.ext.id == ks.id
    }
}
