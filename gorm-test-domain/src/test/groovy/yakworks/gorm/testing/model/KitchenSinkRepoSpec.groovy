package yakworks.gorm.testing.model

import gorm.tools.async.AsyncService
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import gorm.tools.utils.BenchmarkHelper
import spock.lang.Shared

class KitchenSinkRepoSpec extends GormToolsHibernateSpec {
    static int SINK_COUNT = 5000

    AsyncService asyncService
    @Shared KitchenSinkRepo kitchenSinkRepo

    List<Class> getDomainClasses() { [KitchenSink, SinkItem, SinkExt] }

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

    void "items check"() {
        when:
        def ks = KitchenSink.get(1)

        then:
        2 == ks.items.size()
    }
}
