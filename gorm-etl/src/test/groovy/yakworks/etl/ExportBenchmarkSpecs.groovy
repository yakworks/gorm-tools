package yakworks.etl

import java.nio.file.Path

import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic

import gorm.tools.metamap.services.MetaMapService
import gorm.tools.utils.BenchmarkHelper
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.commons.util.BuildSupport
import yakworks.etl.csv.CSVMapWriter
import yakworks.etl.excel.ExcelBuilder
import yakworks.json.groovy.JsonEngineTrait
import yakworks.meta.MetaMapList
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

/**
 * flushing out changes for performance
 */
class ExportBenchmarkSpecs extends Specification implements GormHibernateTest, JsonEngineTrait  {
    static List<Class> entityClasses = [KitchenSink, SinkItem]
    static int SINK_COUNT = 5000

    @Autowired MetaMapService metaMapService

    // @Transactional
    void setupSpec() {
        BenchmarkHelper.startTime()
        KitchenSink.repo.createKitchenSinks(SINK_COUNT)
        BenchmarkHelper.printEndTimeMsg("KitchenSeedData.createKitchenSinks")
    }

    void "sanity check"() {
        expect:
        SINK_COUNT == KitchenSink.count()
        SINK_COUNT == KitchenSink.list().size()
        metaMapService
        // 1000 == sinkList.size()
    }

    //baseline for json to compare, CSV should not take more time
    void "json time"() {
        when:
        Writer jsonWriter = new StringWriter()
        MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*', 'thing.*', 'ext.*', 'simplePogo.foo']) // 'thing.id', 'simplePogo.foo'
        // MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*'])
        BenchmarkHelper.startTime()
        writeJson(jsonWriter, mapList)

        then:
        SINK_COUNT == mapList.size()
        BenchmarkHelper.printEndTimeMsg("writeJson for $SINK_COUNT items")
        writerToFile(jsonWriter, 'testing.json')
        // println writer.toString()
    }


    void "time CSVMapWriter"() {
        when:
        Writer writer = new StringWriter()
        MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*', 'thing.*', 'ext.*', 'simplePogo.foo']) // 'thing.id', 'simplePogo.foo'
        // MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*'])
        BenchmarkHelper.startTime()
        writeCsv(writer, mapList)

        then:
        SINK_COUNT == mapList.size()
        BenchmarkHelper.printEndTimeMsg("CSVMapWriter for $SINK_COUNT items")
        writerToFile(writer, 'testing.csv')
        // println writer.toString()
    }


    void "time writeXlsx"() {
        when:
        // Writer writer = new StringWriter()
        MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*', 'thing.*', 'ext.*', 'simplePogo.foo']) // 'thing.id', 'simplePogo.foo'
        long stime = BenchmarkHelper.startTime()
        ExcelBuilder eb = writeXlsx(mapList)

        then:
        eb.includes.size() == eb.headers.size()
        SINK_COUNT == mapList.size()
        BenchmarkHelper.printEndTimeMsg("writeXlsx for $SINK_COUNT items", stime)
    }

    @CompileStatic
    void writeJson(Writer writer, MetaMapList mapList){
        // Writer swriter = new StringWriter()
        def sjb = new StreamingJsonBuilder(writer, jsonGenerator)
        sjb.call(mapList)
    }

    @CompileStatic
    void writeCsv(Writer writer, MetaMapList mapList){
        def csvMapWriter = CSVMapWriter.of(writer)
        csvMapWriter.writeCsv(mapList as Collection<Map>)

    }

    @CompileStatic
    ExcelBuilder writeXlsx(MetaMapList mapList){
        Path prjPath = BuildSupport.projectPath.resolve("build/testing.xlsx")
        ExcelBuilder eb
        prjPath.withOutputStream { os ->
            eb = ExcelBuilder.of(os)
                .build()
                .writeData(mapList as List<Map>)
                .writeOutAndClose()
        }
        return eb
    }

    @CompileStatic
    Path writerToFile(Writer writer, String filename) {
        Path prjPath = BuildSupport.projectPath.resolve("build/$filename")
        prjPath.setText(writer.toString())
        return prjPath
    }

    @CompileStatic
    String concatenate(String k, String v) {
        "${k}.${v}"
    }

    @CompileStatic
    Map<String, Object> flattenMap(Map<String, Object> map) {
        map.collectEntries { String k, Object v ->
            v instanceof Map ?
                flattenMap(v).collectEntries {  q, r ->
                    def key = concatenate(k, q)
                    [ (key): r ]
                } :
                [ (k): v ]
        }
    }

    Map flattenKeys(Map m, String separator = '.') {
        Set<String> keys = [] as Set<String>
        for (Object entryObj : m.entrySet()) {
            Map.Entry entry = (Map.Entry)entryObj
            put(entry.getKey(), entry.getValue())
        }

        m.each {

        }
        m.collectEntries { k, v ->  v instanceof Map ? flatten(v, separator).collectEntries { q, r ->  [(k + separator + q): r] } : [(k):v] }
    }



}
