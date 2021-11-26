/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import yakworks.commons.map.MetaMapList
import gorm.tools.beans.EntityMapService
import gorm.tools.beans.Pager
import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import testing.Address
import testing.AddyNested
import testing.Cust
import testing.TestSeedData

class PagerSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [Cust, Address, AddyNested] }

    void setupSpec() {
        Cust.withTransaction {
            TestSeedData.buildCustomers(50)
        }
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
        def entityMapService = new EntityMapService()
        def dlist = Cust.list(max: pager.max, offset: pager.offset)
        MetaMapList entityMapList = entityMapService.createEntityMapList(dlist, ["*"])

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
