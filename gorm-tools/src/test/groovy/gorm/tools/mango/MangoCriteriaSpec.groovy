/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import spock.lang.Issue
import testing.TestSource

import java.time.LocalDate

import org.hibernate.QueryException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import testing.Address
import testing.AddyNested
import testing.Cust
import testing.TestIdent
import testing.TestSeedData
import yakworks.testing.gorm.unit.GormHibernateTest

class MangoCriteriaSpec extends Specification implements GormHibernateTest  {
    static List entityClasses = [Cust, Address, AddyNested, TestSource]

    @Autowired MangoBuilder mangoBuilder

    MangoDetachedCriteria build(map, Closure closure = null) {
        //DetachedCriteria detachedCriteria = new DetachedCriteria(Org)
        return mangoBuilder.build(Cust, map, closure)
    }

    void setupSpec() {
        //super.setupSpec()
        Cust.withTransaction {
            TestSeedData.buildCustomers(10)
        }
    }

    void "test field that does not exist"() {
        when:
        List res = build([nonExistingFooBar: true]).list()

        then: "fails with query exception"
        thrown(QueryException)
    }

    void "test non existent association field"() {
        when:
        List res = build(['Type.name': "test"]).list()

        then:
        IllegalArgumentException ex = thrown()
        ex.message.contains "Invalid criteria for field:Type"
    }

    def "test detached isActive"() {
        when:
        List res = build([inactive: true]).list()

        then:
        res.size() == 5
    }

    def "test detached string"() {
        when:
        List res = build([name: "Name1"]).list()

        then:
        res.size() == 1
    }

    def "test enum kind"() {
        when:
        List res = build([kind: "CLIENT"]).list()

        then:
        res.size() == 5

        when:
        res = build([kind: ['CLIENT', 'COMPANY']]).list()

        then:
        res.size() == 10
    }

    def "test enum testIdent"() {
        when:
        List res = build(['testIdent': 'Num2']).list()

        then:
        res.size() == 5

    }

    def "test enum testIdent with id"() {
        when: 'sanity check'
        List res = build(['testIdent': TestIdent.Num2]).list()

        then:
        res.size() == 5

        when:
        res = build(['testIdent': 2]).list()

        then:
        res.size() == 5

        when:
        res = build(['testIdent': [2,4]]).list()

        then:
        res.size() == 10

    }

    def "test enum testIdent with Map"() {
        when: 'sanity check'
        List res = build(['testIdent': TestIdent.Num2]).list()

        then:
        res.size() == 5

        when:
        res = build(['testIdent': [id:2]]).list()

        then:
        res.size() == 5

        when:
        res = build(['testIdent': [[id:2]]]).list()

        then:
        res.size() == 5

    }

    def "test closure"() {
        when:
        List res = build([:],{
            location{
                eq 'address', 'City1'
            }
        }).list()

        then:
        res.size() == 1
    }

    def "test detached like"() {
        when:

        List res = build([name: "Name%"]).list()

        then:
        res.size() == 10
    }

    def "test combined"() {
        when:

        List res = build(([amount: [1 * 1.34, 2 * 1.34, 3 * 1.34, 4 * 1.34], inactive: true])).list()

        then:
        res.size() == 2
    }

    def "test detached BigDecimal"() {
        when:


        List res = build(([amount: 1.34])).list()

        then:
        res.size() == 1

        when:
        res = build(([amount: ['$gt': 6.0]])).list()

        then:
        res.size() == 5
    }

    def "test detached Date"() {
        when:


        List res = build(([date: LocalDate.now().plusDays(2).toDate() ])).list()

        then:
        res.size() == 1

        when:
        res = build(([date: ['$gt': LocalDate.now().plusDays(7).toDate() ]])).list()

        then:
        res.size() == 3
    }

    def "test LocalDate"() {
        when:

        List res = build(([locDate: LocalDate.now().plusDays(2)])).list()

        then:
        res.size() == 1

        when:
        res = build(([locDate: ['$gt': LocalDate.now().plusDays(7)]])).list()

        then:
        res.size() == 3
    }

    def "test LocalDate from string"() {
        when:

        List res = build(([locDate: LocalDate.now().plusDays(2).toString() ])).list()

        then:
        res.size() == 1

        when:
        res = build(([locDate: ['$gt': LocalDate.now().plusDays(7).toString() ]])).list()

        then:
        res.size() == 3
    }

    def "test LocalDateTime"() {
        when:

        List res = build(([locDateTime: LocalDate.now().plusDays(2).atStartOfDay()])).list()

        then:
        res.size() == 1

        when:
        res = build(([locDateTime: ['$gt': LocalDate.now().plusDays(7).atStartOfDay()]])).list()

        then:
        res.size() == 3
    }

    def "test LocalDateTime from string"() {
        when:

        List res = build(([locDateTime: LocalDate.now().plusDays(2).atStartOfDay().toString() ])).list()

        then:
        res.size() == 1

        when:
        res = build(([locDateTime: ['$gt': LocalDate.now().plusDays(7).atStartOfDay().toString() ]])).list()

        then:
        res.size() == 3
    }

    void "query by uuid string"() {
        setup:
        Cust one = Cust.get(1)
        Cust two = Cust.get(2)
        one.uid = UUID.randomUUID()
        two.uid = UUID.randomUUID()
        one.save()
        two.save()
        flush()

        String uid1 = one.uid.toString()
        String uid2 = two.uid.toString()

        when:
        Cust res = build(uid:uid1).get()

        then:
        res
        res == one

        when:
        List results = build(uid:['$in':[uid1, uid2]]).list()

        then:
        results.size() == 2
        results[0] == one
        results[1] == two
    }

    def "test gt"() {
        when:


        List res = build(([id: ['$gt': 4]])).list()

        then:
        res.size() == 6
    }

    def "test ne"() {
        when:


        List res = build(([id: ['$ne': 4]])).list()

        then:
        res.size() == 9
    }

    def "test nested obj"() {
        // when:
        // def loc = Location.get(6)
        // List res = build(locationId: loc.id).list()
        //
        // then:
        // res.size() == 1

        when:
        def loc = Address.get(6)
        def res = build(location: loc).list()

        then:
        res.size() == 1

    }

    def "test nested"() {
        when:

        List res = build("location.id": ['$eq': 6]).list()

        then:
        res.size() == 1
    }

    def "test nested String"() {
        when:

        List res = build((["location.address": "City4"])).list()

        then:
        res.size() == 1
    }

    def "test nested location city"() {
        when:

        List res = build(([
            location: [
                '$or': [
                    address: "City#4",
                    id  : 4
                ]
            ]
        ])).list()

        then:
        res.size() == 1
    }

    def "test nestedId"() {
        when:

        List res = build(locationId: 6 ).list()

        then:
        res.size() == 1
    }

    def "test nestedId when vale is String"() {
        when:

        List res = build(locationId: "6" ).list()

        then:
        res.size() == 1
    }

    @Issue("https://github.com/yakworks/gorm-tools/issues/285")
    void "when id field is not an association"() {
        setup:
        new TestSource(name:"test", source:"s1", sourceId: "sid1").save()
        new TestSource(name:"test2", source:"s2", sourceId: "sid2").save()

        when:
        List l = mangoBuilder.build(TestSource, [sourceId:"sid1"]).list()

        then:
        noExceptionThrown()
        l.size() == 1

        when:
        l = mangoBuilder.build(TestSource, [sourceId:['$ne': 'sid1']]).list()

        then:
        l.size() == 1
        l[0].name == "test2"
    }

    def "test invalid field"() {
        when:

        List res = build((["xxx": ['$eq': 6]])).list()

        then:
        thrown(QueryException)
    }

    def "test or"() {
        when:

        List res = build([
            '$or': [
                [name: "Name7"],
                [id: 2]
            ]
        ]).list()

        then:
        res.size() == 2
    }

    def "test not in list"() {
        when:


        List res = build(([amount: ['$nin': [1 * 1.34, 2 * 1.34, 3 * 1.34, 4 * 1.34]]])).list()

        then:
        res.size() == 6
    }

    def "test in list"() {
        when:


        List res = build(([id: [1, 2, 3, 4]])).list()

        then:
        res.size() == 4
    }

    def "test not"() {
        when:


        List res = build((['$not': [[id: ['$eq': 1]]]])).list()

        then:
        res.size() == 9
    }


    def "test between"() {
        when:


        List res = build(([amount: ['$between': [1 * 1.34, 4 * 1.34]]])).list()

        then:
        res.size() == 4
    }

    def "test isNull/ isNotNull"() {
        when:


        List res = build(([name2: ['$isNull': true]])).list()

        then:
        res.size() == 5

        when:

        res = build(([name2: '$isNull'])).list()

        then:
        res.size() == 5
    }

    def "test fields comparison"() {
        when:

        List res = build(([amount: ['$gtef': "amount2"]])).list()

        then:
        res.size() == 5

        when:
        res = build(([amount: ['$gtf': "amount2"]])).list()

        then:
        res.size() == 4

        when:
        res = build(([amount: ['$ltf': "amount2"]])).list()

        then:
        res.size() == 5

        when:
        res = build(([amount: ['$eqf': "amount2"]])).list()

        then:
        res.size() == 1

        when:
        res = build(([amount: ['$nef': "amount2"]])).list()

        then:
        res.size() == 9
    }

    def "test qSearch"() {
        when:

        List res = build((['$qSearch': "Na"])).list()

        then:
        res.size() == 10

        when:

        res = build((['$qSearch': "Name3"])).list()

        then:
        res.size() == 1

        when: "quick search is combined with another field"
        res = build((['$qSearch': "Nam%", inactive: true])).list()

        then:
        res.size() == 5

        when: 'quickserach has fields under $q'
        res = build((['$qSearch': ['text': "Nam", 'fields': ['name']], inactive: true])).list()

        then:
        res.size() == 5

        when: 'quickserach has fields under $qSearch'
        res = build((['$qSearch': ['text': "Nam", 'fields': ['name']], inactive: true])).list()

        then:
        res.size() == 5

    }

    def "test with closure"() {
        when:


        List res = build([name: "Name%"]) { gt "id", 5 }.list()

        then:
        res.size() == 5
    }


    def "test with deep nested"() {
        when:
        List res = build((["location.nested.name": "Nested4"])).list()

        then:
        res.size() == 1
    }

    def "test with `or` on one level"() {
        when:
        List res = build((['$or': [["location.id": 5], ["name": "Name1", "location.id": 4]]])).list()

        then:
        res.size() == 1
    }

    def "test order with closure"() {
        when:
        List res = build([:],{
            // location {
            //     order("address", "desc")
            // }
            order("location.address", "desc")
        }).list()

        then:
        res[0].location.address > res[1].location.address
    }

    def "test order simple"() {
        when:
        List res = build(['$sort': 'descId']).list()

        then: 'sanity check first few'
        res[0].descId < res[1].descId
        res[1].descId < res[2].descId
    }

    def "test order desc"() {
        when:
        List res = build('$sort': [id: "desc"]).list()

        then:
        res[0].id > res[1].id
        res[1].id > res[2].id
    }

    def "test order association"() {
        when:
        List res = build('$sort':['location.address': 'desc'] ).list()

        then:
        res[0].location.address > res[1].location.address
        // res[1].id > res[2].id
    }

    def "test order multi"() {
        when:
        List res = build('$sort': [inactive: "desc", id: "desc"]).list()

        then:
        // inactive is split 50/50 true false in test data, first 5 should be true
        res[0].inactive == res[4].inactive
        res[0].id > res[1].id
        res[4].inactive > res[5].inactive
        res[5].id > res[6].id
    }

    def "test multisort string"() {
        when:
        List res = build('$sort': 'inactive desc, id desc').list()
        then:
        res[0].inactive == res[4].inactive
        res[0].id > res[1].id
        res[4].inactive > res[5].inactive
        res[5].id > res[6].id
    }

}
