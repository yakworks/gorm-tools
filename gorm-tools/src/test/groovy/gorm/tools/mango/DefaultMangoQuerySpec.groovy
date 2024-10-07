/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import testing.Address
import testing.AddyNested
import testing.Cust
import testing.CustRepo
import testing.TestSeedData
import yakworks.api.problem.data.DataProblemException
import yakworks.testing.gorm.unit.GormHibernateTest

class DefaultMangoQuerySpec extends Specification implements GormHibernateTest {
    static List entityClasses = [Cust, Address, AddyNested]

    @Autowired CustRepo custRepo

    DefaultQueryService getMangoQuery(){
        custRepo.queryService
    }

    void setupSpec() {
        Cust.withTransaction {
            TestSeedData.buildCustomers(100)
        }
    }


    def "count"() {
        when:
        int count = mangoQuery.query( [name: 'Name1']).count() as Integer

        then:
        count

        when:
        count = mangoQuery.query([name: 'FOOBAR']).count() as Integer

        then:
        !count
    }

    def "exists"() {
        when:
        boolean hasIt = mangoQuery.query([name: 'Name1']).exists()

        then:
        hasIt

        when:
        hasIt = mangoQuery.query([name: 'FOOBAR']).exists()

        then:
        !hasIt
    }

    def "sort check"() {
        when: "Check if \$sort will cause NullPointerException"
        def list = mangoQuery.query([name: 'joe', '$sort': 'id'])
        then:
        noExceptionThrown()
        list != null

        when: "Check if sort will cause NullPointerException"
        list = mangoQuery.query([name: 'joe', 'sort': 'id'])
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

    void "invalid type"() {
        when:
        def query = Cust.query(uid:['$eq': 1])

        then:
        DataProblemException ex = thrown()
        ex.code == 'error.data.problem'
        ex.detail.contains 'Invalid query string - Cannot cast object'
    }

    void "test non existent association field"() {
        when:
        def res =  Cust.query("Missing.name":"test")

        then:
        DataProblemException ex = thrown()
        ex.message.contains "Invalid query string"
    }

    void "test invalid date"() {
        when:
        List res =  Cust.query("locDate":"xxx")

        then:
        DataProblemException ex = thrown()
        ex.message.contains "Invalid query string"
    }

}
