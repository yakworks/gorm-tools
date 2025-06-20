package yakworks.etl

import gorm.tools.metamap.services.MetaMapService
import gorm.tools.utils.BenchmarkHelper
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.meta.MetaMapList
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

class CSVWriterSpec extends Specification implements GormHibernateTest {
    static int SINK_COUNT = 5000
    private Writer writer = new StringWriter()

    @Autowired MetaMapService metaMapService

    static List entityClasses = [KitchenSink, SinkItem]

    // @Transactional
    void setupSpec() {
        BenchmarkHelper.startTime()
        KitchenSink.repo.createKitchenSinks(SINK_COUNT)
        BenchmarkHelper.printEndTimeMsg("KitchenSeedData.createKitchenSinks")
    }

    // @Transactional
    // void cleanupSpec() {
    //     KitchenSink.deleteAll()
    // }

    void "sanity check"() {
        expect:
        SINK_COUNT == KitchenSink.count()
        SINK_COUNT == KitchenSink.list().size()
        metaMapService
        // 1000 == sinkList.size()
    }

    void "time CSVMapWriter"() {
        when:
        BenchmarkHelper.startTime()
        MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*', 'ext.*', 'thing.*', 'simplePogo.*'])
        // MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*'])
        def csvMapWriter = CSVMapWriter.of(writer)
        csvMapWriter.createHeader(mapList).writeCsv(mapList)

        then:
        SINK_COUNT == mapList.size()
        BenchmarkHelper.printEndTimeMsg("CSVMapWriter for $SINK_COUNT items")
    }


}
