package gorm.tools.csv

import java.text.DecimalFormat

import gorm.tools.beans.Pager
import gorm.tools.csv.render.CSVMapWriter
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import gorm.tools.utils.BenchmarkHelper
import grails.gorm.transactions.Transactional
import yakworks.gorm.testing.model.KitchenSeedData
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkItem
import yakworks.meta.MetaMapList

class CSVWriterSpec extends GormToolsHibernateSpec {

    private Writer writer = new StringWriter()

    MetaMapService metaMapService

    List<Class> getDomainClasses() { [KitchenSink, SinkItem] }

    // @Transactional
    void setupSpec() {
        BenchmarkHelper.startTime()
        KitchenSeedData.createKitchenSinks(1000)
        BenchmarkHelper.printEndTimeMsg("KitchenSeedData.createKitchenSinks")
    }

    // @Transactional
    // void cleanupSpec() {
    //     KitchenSink.deleteAll()
    // }

    void "sanity check"() {
        expect:
        1000 == KitchenSink.count()
        1000 == KitchenSink.list().size()
        metaMapService
        // 1000 == sinkList.size()
    }

    void "time CSVMapWriter"() {
        when:
        BenchmarkHelper.startTime()
        MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*'])
        def csvMapWriter = CSVMapWriter.of(writer)
        csvMapWriter.writeCsv(mapList)

        then:
        1000 == mapList.size()
        BenchmarkHelper.printEndTimeMsg("CSVMapWriter for 1000 items")
    }

    //copied  from rest EntityResponder
    Pager pagedQuery(Map params, List<String> includesKeys) {
        Pager pager = new Pager(params)
        List dlist = query(pager, params)
        List<String> incs = findIncludes(params, includesKeys)
        MetaMapList entityMapList = metaMapService.createMetaMapList(dlist, incs)
        return pager.setEntityMapList(entityMapList)
    }

    // Closure doWithDomains() { { ->
    //     syncJobService(TestSyncJobService)
    // }}
    //
    // void "writer test"() {
    //     when:
    //
    //     def csvMapWriter = CSVMapWriter.of(writer)
    //     csvMapWriter.writeCsv()
    //
    //     then:
    //     1000 == KitchenSink.count()
    //     // 1000 == sinkList.size()
    // }

}
