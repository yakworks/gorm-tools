package gorm.tools.csv

import java.text.DecimalFormat

import gorm.tools.beans.Pager
import gorm.tools.csv.render.CSVMapWriter
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import gorm.tools.utils.BenchmarkHelper
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkItem
import yakworks.meta.MetaMapList

class CSVWriterSpec extends GormToolsHibernateSpec {
    static int SINK_COUNT = 5000
    private Writer writer = new StringWriter()

    MetaMapService metaMapService

    List<Class> getDomainClasses() { [KitchenSink, SinkItem] }

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
        // MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*', 'ext.*', 'thing.*', 'simplePogo.*'])
        MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*'])
        def csvMapWriter = CSVMapWriter.of(writer)
        csvMapWriter.writeCsv(mapList)

        then:
        SINK_COUNT == mapList.size()
        BenchmarkHelper.printEndTimeMsg("CSVMapWriter for $SINK_COUNT items")
    }

    //copied  from rest EntityResponder
    Pager pagedQuery(Map params, List<String> includesKeys) {
        Pager pager = new Pager(params)
        List dlist = query(pager, params)
        List<String> incs = findIncludes(params, includesKeys)
        MetaMapList entityMapList = metaMapService.createMetaMapList(dlist, incs)
        return pager.setEntityMapList(entityMapList)
    }

}