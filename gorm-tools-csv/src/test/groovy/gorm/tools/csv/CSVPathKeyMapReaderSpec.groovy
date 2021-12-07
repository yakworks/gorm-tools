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

}
