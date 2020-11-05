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
        Map parsed = mangoQuery.parseParams([criteria: [id: 24], max: 10, sort:'foo', page: 2, offset: 10])
        then:
        //parsed.criteria.id == 24
        parsed.criteria == [id: 24, '$sort': 'foo']
        parsed.pager == [max: 10, offset: 10, page: 2]

        when: 'q is used and reserved pager is passed'
        parsed = mangoQuery.parseParams(q: [id: 24, offset: 'testing'], max: 10, sort:'foo', page: 2, offset: 10)

        then: 'it should have them in criteria'
        //parsed.criteria.id == 24
        parsed.criteria == [id: 24, offset: 'testing', '$sort': 'foo']
        parsed.pager == [max: 10, offset: 10, page: 2]

        when: 'not using q or criteria'
        parsed = mangoQuery.parseParams(name: 'joe', max: 10, sort:'foo', page: 2, offset: 10)

        then:
        parsed['criteria'] == [name: 'joe', '$sort': 'foo']
        parsed['pager'] == [max: 10, page: 2, offset: 10]

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

    void "query eq check"() {
        when:
        def qlist = Org.query {
            eq 'location.id', 1l
        }.list()

        then:
        qlist.size() == 1

        when:
        qlist = Org.query {
            eq 'location.address', 'City1'
        }.list()

        then:
        qlist.size() == 1

        when:
        qlist = Org.query {
            invokeMethod('location') {
                eq 'address', 'City1'
            }
        }.list()

        then:
        qlist.size() == 1

        when:
        qlist = Org.query {
            assoc('location') {
                eq 'address', 'City1'
            }
        }.list()

        then:
        qlist.size() == 1

        // when:
        // qlist = Org.query {
        //     assoc('location') {
        //         assoc('nested') {
        //             eq 'value', 1.0
        //         }
        //     }
        // }.list()
        //
        // then:
        // qlist.size() == 1
    }

    void "query inlist check"() {
        when:
        def qlist = Org.query {
            inList 'id', [1l, 2l]
        }.list()

        then:
        qlist.size() == 2

        when:
        qlist = Org.query {
            inList 'location.address', ['City1', 'City2']
        }.list()

        then:
        qlist.size() == 2

    }

    void "query get check"() {
        when:
        def o = Org.query {
            eq 'location.id', 1l
        }.get()

        then:
        o.id == 1

        when:
        o = Org.query {
            eq 'location.address', 'City2'
        }.get()

        then:
        o.id == 2
        o.location.address == 'City2'

    }

}
