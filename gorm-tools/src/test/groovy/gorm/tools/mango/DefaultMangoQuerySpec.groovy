/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.buildtestdata.TestData
import grails.gorm.DetachedCriteria
import grails.test.hibernate.HibernateSpec
import grails.testing.spring.AutowiredTest
import testing.Location
import testing.Nested
import testing.Org
import testing.OrgType
import testing.TestSeedData

class DefaultMangoQuerySpec extends GormToolsHibernateSpec implements AutowiredTest {

    DefaultMangoQuery mangoQuery

    List<Class> getDomainClasses() { [Org, Location, Nested] }

    void setupSpec() {
        Org.withTransaction {
            TestSeedData.buildOrgs(100)
        }
    }

    def "parseParams"() {
        when:
        def parsed = mangoQuery.parseParams([criteria: [id: 24], max: 10, sort:'foo', page: 2, offset: 10])
        then:
        parsed['criteria'] == [id: 24, '$sort': 'foo']
        parsed['pager'] == [max: 10, offset: 10, page: 2]

        when:
        parsed = mangoQuery.parseParams(name: 'joe', max: 10, sort:'foo', page: 2)
        then:
        parsed['criteria'] == [name: 'joe', '$sort': 'foo']
        parsed['pager'] == [max: 10, page: 2]

        when:
        parsed = mangoQuery.parseParams(name: 'joe')
        then:
        parsed['criteria'] == [name: 'joe']
        !parsed['pager']
    }

    def "sort check"() {
        when: "Check if \$sort will cause NullPointerException"
        def list = mangoQuery.query(Org, [name: 'joe', '$sort': 'id'])
        then:
        noExceptionThrown()
        list != null

        when: "Check if sort will cause NullPointerException"
        list = mangoQuery.query(Org, [name: 'joe', 'sort': 'id'])
        then:
        noExceptionThrown()
        list != null
    }

}
