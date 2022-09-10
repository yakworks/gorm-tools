/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import yakworks.gorm.testing.hibernate.GormToolsHibernateSpec
import grails.testing.spring.AutowiredTest
import testing.Address
import testing.AddyNested
import testing.Cust
import testing.TestSeedData

class DefaultMangoQuerySpec extends GormToolsHibernateSpec implements AutowiredTest {

    DefaultMangoQuery mangoQuery

    List<Class> getDomainClasses() { [Cust, Address, AddyNested] }

    void setupSpec() {
        Cust.withTransaction {
            TestSeedData.buildCustomers(100)
        }
    }

    def "sort check"() {
        when: "Check if \$sort will cause NullPointerException"
        def list = mangoQuery.query(Cust, [name: 'joe', '$sort': 'id'])
        then:
        noExceptionThrown()
        list != null

        when: "Check if sort will cause NullPointerException"
        list = mangoQuery.query(Cust, [name: 'joe', 'sort': 'id'])
        then:
        noExceptionThrown()
        list != null
    }

    void "query eq check"() {
        when:
        def qlist = Cust.query {
            eq 'location.id', 1l
        }.list()

        then:
        qlist.size() == 1

        when:
        qlist = Cust.query {
            eq 'location.address', 'City1'
        }.list()

        then:
        qlist.size() == 1

        when:
        qlist = Cust.query {
            invokeMethod('location') {
                eq 'address', 'City1'
            }
        }.list()

        then:
        qlist.size() == 1

        when:
        qlist = Cust.query {
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
        def qlist = Cust.query {
            inList 'id', [1l, 2l]
        }.list()

        then:
        qlist.size() == 2

        when:
        qlist = Cust.query {
            inList 'location.address', ['City1', 'City2']
        }.list()

        then:
        qlist.size() == 2

    }

    void "query get check"() {
        when:
        def o = Cust.query {
            eq 'location.id', 1l
        }.get()

        then:
        o.id == 1

        when:
        o = Cust.query {
            eq 'location.address', 'City2'
        }.get()

        then:
        o.id == 2
        o.location.address == 'City2'

    }

}
