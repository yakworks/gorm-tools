package gorm.tools.csv

import java.nio.file.Path

import groovy.transform.CompileStatic

import gorm.tools.beans.Pager
import gorm.tools.csv.render.CSVMapWriter
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import gorm.tools.utils.BenchmarkHelper
import org.apache.commons.beanutils.PropertyUtils
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import spock.lang.Shared
import yakworks.commons.lang.PropertyTools
import yakworks.commons.util.BuildSupport
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkItem
import yakworks.meta.MetaMap
import yakworks.meta.MetaMapList

/**
 * flushing out changes for performance
 */
class CSVWriterBenchmarkSpec extends GormToolsHibernateSpec {
    static int SINK_COUNT = 5000
    @Shared Writer writer = new StringWriter()

    MetaMapService metaMapService

    List<Class> getDomainClasses() { [KitchenSink, SinkItem] }

    // @Transactional
    void setupSpec() {
        BenchmarkHelper.startTime()
        KitchenSink.repo.createKitchenSinks(SINK_COUNT)
        BenchmarkHelper.printEndTimeMsg("KitchenSeedData.createKitchenSinks")
    }

    // @Transactional
    void cleanupSpec() {
        // println writer.toString()
    }

    void "sanity check"() {
        expect:
        SINK_COUNT == KitchenSink.count()
        SINK_COUNT == KitchenSink.list().size()
        metaMapService
        // 1000 == sinkList.size()
    }

    void "time CSVMapWriter"() {
        when:
        MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*', 'thing.*', 'ext.*', 'simplePogo.foo']) // 'thing.id', 'simplePogo.foo'
        // MetaMapList mapList = metaMapService.createMetaMapList(KitchenSink.list(), ['*'])
        BenchmarkHelper.startTime()
        writeCsv(mapList)

        then:
        SINK_COUNT == mapList.size()
        BenchmarkHelper.printEndTimeMsg("CSVMapWriter for $SINK_COUNT items")

        // println writer.toString()
    }

    @CompileStatic
    void writeCsv(MetaMapList mapList){
        def csvMapWriter = CSVMapWriter.of(writer)
        // csvMapWriter.writeCsv(dataList)
        // Map<String, Object> firstRow = dataList[0] as Map<String, Object>
        Set<String> headers = mapList.metaEntity.flattenProps()
        println "headers $headers"
        csvMapWriter.writeHeaders(headers)

        mapList.eachWithIndex{ row, int i->
            //get all the values for the masterHeaders keys
            // csvMapWriter.writeLine(flattenMap(row as Map<String, Object>))
            // Map rowMap = CSVMapWriter.flattenMap(row as Map<String, Object>) //row as Map<String, Object>
            List vals1 = collectVals(row as Map, headers)
            // def vals1 = headers.collect{ row[it] as String }
            // def vals = (row as MetaMap).values()
            csvMapWriter.writeCollection(vals1)

            //flush every 1000
            if (i % 1000 == 0) {
                csvMapWriter.flush()
            }
        }

        Path prjPath = BuildSupport.gradleProjectPath.resolve("build/testing.csv")
        prjPath.setText(writer.toString())

    }

    @CompileStatic
    List collectVals(Map map, Set<String> headers) {
        List vals = headers.collect {
            PropertyUtils.getProperty(map, it)
            // PropertyTools.getProperty(map, it)
        }
        return vals
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
