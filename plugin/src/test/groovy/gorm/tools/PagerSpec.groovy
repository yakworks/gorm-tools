package gorm.tools

import gorm.tools.testing.unit.GormToolsTest
import grails.persistence.Entity
import spock.lang.Specification

class PagerSpec extends Specification implements GormToolsTest {

    void setupSpec() {
        //mockDomain Person
        mockDomains ClassB
    }

    def "test default values"() {
        when:
        Pager pager = new Pager()

        then:
        pager.max == 10
        pager.page == 1
        pager.recordCount == 0
        pager.data == null

    }

    def "test setting params"() {
        when:
        Pager pager = new Pager([page: 3, max: 20])

        then:
        pager.max == 20
        pager.page == 3
        pager.offset == 40
        pager.recordCount == 0
        pager.data == null

        when:
        pager.setupData(40..50 as List)
        Map jsonData = pager.getJsonData()

        then:
        pager.recordCount == 11
        jsonData.page == 3
        jsonData.total == 1
        jsonData.records == 11
        jsonData.rows == 40..50 as List
    }

    def "test setupData with fields"() {
        setup:
        Pager pager = new Pager()
        50.times {
            new ClassB(
                value: 5 * it
            ).save(failOnError: true)
        }
        when:
        pager.setupData(ClassB.list(max: pager.max, offset: pager.offset), ["*"])
        Map jsonData = pager.jsonData
        then:
        jsonData.page == 1
        jsonData.records == 50
        jsonData.total == 5
        jsonData.rows == (0..9 as List).collect { [id: it + 1, value: 5 * (it), version: 0] }
    }

    def "test eachPage"() {
        setup:
        Pager paginator = new Pager()
        paginator.max = 10
        paginator.recordCount = 95
        List pages = []

        when:
        paginator.eachPage { def max, def offset ->
            pages << [max: max, offset: offset]
        }

        then:
        10 == pages.size()
        10 == pages[0].max
        0 == pages[0].offset
        90 == pages[9].offset
        10 == pages[9].max
    }
}


@Entity
class ClassB {
    int value
    int version = 0
}
