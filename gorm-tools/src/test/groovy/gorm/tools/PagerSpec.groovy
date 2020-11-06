/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import gorm.tools.beans.EntityMapList
import gorm.tools.beans.EntityMapService
import gorm.tools.beans.Pager
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

        then: 'defaults should be as follows'
        pager.max == 20
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

    }

    def "test setupData with fields"() {
        setup:
        Pager pager = new Pager()
        50.times {
            new ClassB(
                value: 5 * it
            ).save(failOnError: true)
        }
        def entityMapService = new EntityMapService()
        def dlist = ClassB.list(max: pager.max, offset: pager.offset)
        EntityMapList entityMapList = entityMapService.createEntityMapList(dlist, ["*"])

        when:
        pager.setEntityMapList(entityMapList)

        then:
        pager.page == 1
        pager.recordCount == 50
        pager.pageCount == 3
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
