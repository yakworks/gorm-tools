package gorm.tools.csv

import gorm.tools.databinding.PathKeyMap
import spock.lang.Shared
import spock.lang.Specification

class CSVPathKeyMapReaderSpec extends Specification {

    @Shared File kitchenSinkCsv, sinkItemCsv

    def setup() {
        kitchenSinkCsv = new File("src/test/resources/KitchenSink.short.csv")
        sinkItemCsv = new File("src/test/resources/Sink.Item.short.csv")
    }

    void "files exists"() {
        expect:
        kitchenSinkCsv.exists()
        sinkItemCsv.exists()
    }

    void "test simple read rows"() {
        when:
        CSVPathKeyMapReader csvReader = new CSVPathKeyMapReader(new FileReader(kitchenSinkCsv))
        List data = csvReader.readAllRows()

        then:
        data[0] instanceof PathKeyMap
        data[0].name == "red"
        data[0].num == "sink1"

        data[1].name == "yellow"
        data[1].num == "sink2"

        data[2].name == "green"
        data[2].num == "sink3"
    }

    void "test read with closure"() {
        when:
        CSVPathKeyMapReader csvReader = new CSVPathKeyMapReader(new FileReader(kitchenSinkCsv))
        List data = []

        while (csvReader.hasNext()) {
            Map row = csvReader.readMap { PathKeyMap m ->
                m.name = m.name + "1"
            }
            data << row
        }

        then:
        data[0] instanceof PathKeyMap
        data[0].name == "red1"
        data[0].num == "sink1"

        data[1].name == "yellow1"
        data[1].num == "sink2"

        data[2].name == "green1"
        data[2].num == "sink3"
    }

    void "merge two csv into single map structure"() {
        setup:
        CSVPathKeyMapReader kitchenSinkReader = new CSVPathKeyMapReader(new FileReader(kitchenSinkCsv))
        CSVPathKeyMapReader sinkItemReader = new CSVPathKeyMapReader(new FileReader(sinkItemCsv), "_")

        when:
        List<Map> kitchenSinks = kitchenSinkReader.readAllRows()
        List<Map> sinkItems = []
        while(sinkItemReader.hasNext()) {
            Map row = sinkItemReader.readMap() { PathKeyMap m ->
                if(!m) return
                String kitchenSinkNum = m.kitchenSink.num
                if(kitchenSinkNum) {
                    Map kc = kitchenSinks.find({ it.num == kitchenSinkNum})
                    if(kc) {
                        m.kitchenSink = kc
                        if(!kc.items) kc.items = []
                        kc.items << m
                    }
                }
            }

            if(row) sinkItems << row
        }

        then:
        kitchenSinks.size() == 3
        sinkItems.size() == 14

        and: "verfiy grouping"
        kitchenSinks[0].num == "sink1"
        kitchenSinks[0].items != null
        kitchenSinks[0].items.size() == 1 //one sink item for this kitchen sink

        kitchenSinks[1].num == "sink2"
        kitchenSinks[1].items != null
        kitchenSinks[1].items.size() == 2

        kitchenSinks[2].num == "sink3"
        kitchenSinks[2].items != null
        kitchenSinks[2].items.size() == 11
    }

}
