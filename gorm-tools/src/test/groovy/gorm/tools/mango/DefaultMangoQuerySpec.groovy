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
        Cust.query(uid:['$eq': 1])

        then:
        DataProblemException ex = thrown()
        ex.code == 'error.query.invalid'
        //ex.message == "Invalid query"
        ex.detail.contains "Cannot cast object '1' with class 'java.lang.Integer' to class 'java.util.UUID"
    }

    void "test non existent association field"() {
        when:
        Cust.query("foo.name":"test")

        then:
        DataProblemException ex = thrown()
        ex.code == 'error.query.invalid'
        ex.detail.contains("Invalid criteria for field:foo")
    }

    void "test invalid date"() {
        when:
        Cust.query("locDate":"xxx")

        then:
        DataProblemException ex = thrown()
        ex.code == 'error.query.invalid'
        ex.detail.contains("Text 'xxx' could not be parsed")
    }

    void "query fails with class cast exception"() {
        when:
        Cust.query("location":[[address:['add 1']]]).list()

        then:
        DataProblemException ex = thrown()
        ex.code == 'error.query.invalid'
        ex.detail.contains "java.util.ArrayList cannot be cast to class java.lang.String"
    }

}
