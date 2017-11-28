package daoapp

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.test.mixin.integration.Integration
import org.grails.web.json.JSONElement
import spock.lang.Shared
import spock.lang.Specification

@Integration
class OrgSpec extends Specification {

    def "Check list"() {
        expect:
        Org.dao.list().size() == 10
    }

    def "Filter by Name eq"() {
        when:
        List list = Org.dao.list([criteria:[name: "Org#23"],max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#23"
    }

    def "Filter by id eq"() {
        when:
        List list = Org.dao.list([criteria:[id: 24], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#23"
    }

    def "Filter by id inList"() {
        when:
        List list = Org.dao.list([criteria:[id: [24, 25]], max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }

    def "Filter by Name ilike"() {
        when: "eq"
        List list = Org.dao.list([criteria:[name: "Org#2%"], max: 150])
        then:
        list.size() == 11
        list[0].name == "Org#2"
        list[1].name == "Org#20"
        list[10].name == "Org#29"
    }

    def "Filter by nested id"() {
        when: "eq"
        List list = Org.dao.list([criteria:[address: [id: 2]], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
        list[0].address.id == 2
    }

    def "Filter by nestedId"() {
        when: "eq"
        List list = Org.dao.list([criteria:["address.id": 2], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
        list[0].address.id == 2
    }

    def "Filter by nested id inList"() {
        when:
        List list = Org.dao.list([criteria:[address:[id: [24, 25, 26]]], max: 150])
        then:
        list.size() == 3
        list[0].name == "Org#23"
    }

    def "Filter by nested string"() {
        when: "eq"
        List list = Org.dao.list([criteria:[address: [city: "City#2"]], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#2"
        list[0].address.id == 3
    }

    def "Filter by nested string ilike"() {
        when: "eq"
        List list = Org.dao.list([criteria:[address: [city: "City#2%"]], max: 150])
        then:
        list.size() == 11
        list[0].name == "Org#2"
        list[1].name == "Org#20"
        list[10].name == "Org#29"
        list[0].address.id == 3
        list[1].address.id == 21
        list[10].address.id == 30
    }

    def "Filter by boolean"() {
        when:
        List list = Org.dao.list([criteria:[isActive: true], max: 150])
        then:
        list.size() ==  Org.createCriteria().list(){eq "isActive", true}.size()
    }

    def "Filter by boolean in list"() {
        when:
        List list = Org.dao.list([criteria:[isActive: [false]], max: 150])
        then:
        list.size() == 50
        list[0].isActive == false
        list[1].isActive == false
    }

    def "Filter by BigDecimal"() {
        when:
        List list = Org.dao.list([criteria:[revenue: 200.0], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#2"
    }

    def "Filter by BigDecimal in list"() {
        when:
        List list = Org.dao.list([criteria:[revenue: [200.0, 500.0]], max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#2"
        list[1].name == "Org#5"
    }

    def "Filter by Date"() {
        when:
        List list = Org.dao.list([criteria:[testDate: (new Date() +1).clearTime()], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }

    def "Filter by Date le"() {
        when:
        List list = Org.dao.list([criteria:['testDate.$lte': (new Date() +1).clearTime()], max: 150])
        then:
        list.size() == Org.createCriteria().list(){le "testDate", (new Date() +1).clearTime()}.size()
        list[0].name == Org.createCriteria().list(){le "testDate", (new Date() +1).clearTime()}[0].name
    }

    def "Filter by xxxId 1"() {
        when: "xxxId in domain"
        List list = Org.dao.list([criteria:[refId: 200], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }
    def "Filter by xxxId 2"() {

        when: "xxxId in nested domain"
        List list = Org.dao.list([criteria:["address.testId": 9], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#3"
    }


    def "Filter by xxxId 3"(){
        when: "xxxId in nested domain2"
        List list = Org.dao.list([criteria:[address:[testId: 3]], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }

    def "Filter by xxxId 4"() {
        when: "xxxId in nested domain"
        List list = Org.dao.list([criteria:["address.testId": [9, 12]], max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#3"
    }

    def "Filter by xxxId 4 criteria as JSON"() {
        when:
        Map params = [max: 150]
        params.criteria = (["address.testId": [9, 12]] as grails.converters.JSON).toString()
        List list = Org.dao.list(params)
        then:
        list.size() == 2
        list[0].name == "Org#3"
    }


    def "Filter with `or` "(){
        when:
        List list = Org.dao.list([criteria:['$or': ["name": "Org#1", "address.id": 4 ]], max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#1"
        list[1].name == "Org#3"
    }

    def "Filter with `or` on low level"(){
        when:
        List list = Org.dao.list([criteria:[address: ['$or':["city": "City#1", "id": 4 ]]], max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#1"
        list[1].name == "Org#3"
    }

    def "Filter with several `or` on one level"(){
        when:
        println "Filter with several `or` on one level"
        List list = Org.dao.list([criteria:['$or': [["address.id": 5 ], ["name": "Org#1", "address.id": 4 ]]], max: 150])
        then:
        list.size() == Org.createCriteria().list() {
            or {
                address {
                    eq "id", 5L
                }
                and {
                    eq "name", "Org#1"
                    address {
                        eq "id", 4L
                    }
                }
            }
        }.size()
        list[0].name == "Org#4"
    }

    def "Filter with several `or` on one level2"(){
        when:
        List list = Org.dao.list([criteria:["\$or": [["address.id": 5 ], [ "address.id": 4 ]]], max: 150])
        then:
        list.size() == Org.createCriteria().list() {
            or {
                address {
                    eq "id", 5L
                }
                address { eq "id", 4L

                }
            }
        }.size()
        list.size() == 2
        list[1].name == "Org#4"
    }

    def "Filter with `or` with like"(){
        when:
        List list = Org.dao.list([criteria:["\$or": ["name": "Org#2%", "address.id": 4 ]], max: 150]).sort{it.id}
        then:
        list.size() == 12
        list[0].name == "Org#2"
        list[1].name == "Org#3"
        list[2].name == "Org#20"
    }

    def "Filter with `between()`"(){
        when:
        List list = Org.dao.list([criteria:[id: ["\$between": [2, 10]]], max: 150]).sort{it.id}
        then:
        list.size() == 9
        list[0].name == "Org#1"
        list[1].name == "Org#2"
        list[-1].name == "Org#9"
    }

    def "Filter with `in()`"(){
        when:
        List list = Org.dao.list([criteria:[id: ["\$in": [24, 25]]], max: 150]).sort{it.id}
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }
    def "Filter with `inList()`"(){
        when:
        List list = Org.dao.list([criteria:[id: ["\$inList":[24, 25]]], max: 150]).sort{it.id}
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }
    def "Filter by Name ilike()"() {
        when:
        List list = Org.dao.list([criteria:[name:["\$ilike": "Org#2%"]], max: 150])
        then:
        list.size() == 11
        list[0].name == "Org#2"
        list[1].name == "Org#20"
        list[10].name == "Org#29"
    }

    def "Filter with `gt()`"(){
        when:
        List list = Org.dao.list([criteria:[id: ["\$gt": 95]], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){gt "id", 95L}.size()
        list[0].name == Org.createCriteria().list(){gt "id", 95L}[0].name
    }

    def "Filter with `ge()`"(){
        when:
        List list = Org.dao.list([criteria:[id: ['\$gte': 95]], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){ge "id", 95L}.size()
        list[0].name == Org.createCriteria().list(){ge "id", 95L}[0].name
    }

    def "Filter with `ge()` for bigdecimal"(){
        when:
        List list = Org.dao.list([criteria:[revenue: ["\$gte": 9500.0]], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){ge "revenue", new BigDecimal("9500")}.size()
        list[0].name == Org.createCriteria().list(){ge "revenue", new BigDecimal("9500")}[0].name
    }

    def "Filter with `lte()` then another value"(){
        when:
        List list = Org.dao.list([criteria:[revenue: ["\$ltef": "credit"]], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){leProperty "revenue", "credit"}.size()
        list[0].name == Org.createCriteria().list(){leProperty "revenue", "credit"}[0].name
    }

    def "Filter with `gte()` then another value"(){
        when:
        List list = Org.dao.list([criteria:[revenue: ["\$gtef": "credit"]], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){geProperty "revenue", "credit"}.size()
        list[0].name == Org.createCriteria().list(){geProperty "revenue", "credit"}[0].name
    }

    def "Filter with `gt()` then another value"(){
        when:
        List list = Org.dao.list([criteria:[revenue: ["\$gtf": "credit"]], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){gtProperty "revenue", "credit"}.size()
        list[0].name == Org.createCriteria().list(){gtProperty "revenue", "credit"}[0].name
    }

    def "Filter with `lt()` then another value"(){
        when:
        List list = Org.dao.list([criteria:[revenue: ["\$ltf": "credit"]], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){ltProperty "revenue", "credit"}.size()
        list[0].name == Org.createCriteria().list(){ltProperty "revenue", "credit"}[0].name
    }

    def "Filter with `lt()`"(){
        when:
        List list = Org.dao.list([criteria:[id: ["\$lt": 5]], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){lt "id", 5L}.size()
        list[0].name == Org.createCriteria().list(){lt "id", 5L}[0].name
    }

    def "Filter with `le()`"(){
        when:
        List list = Org.dao.list([criteria:[id: ["\$lte": 5]], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){le "id", 5L}.size()
        list[0].name == Org.createCriteria().list(){le "id", 5L}[0].name
    }

    def "Filter with `isNull` object"(){
        when:
        List list = Org.dao.list([criteria:[credit: ["\$isNull": true]], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){isNull "credit"}.size()
    }

    def "Filter with `isNull` when val"(){
        when:
        List list = Org.dao.list([criteria:[credit: "\$isNull"], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){isNull "credit"}.size()
    }

    def "Filter with `isNull` when just 'null'"(){
        when:
        List list = Org.dao.list([criteria:[credit: null], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){isNull "credit"}.size()
    }

    def "Filter with `isNull` when just null"(){
        when:
        List list = Org.dao.list([criteria:[credit: null], max: 150]).sort{it.id}
        then:
        list.size() == Org.createCriteria().list(){isNull "credit"}.size()
    }


    def "Filter with `not in()`"(){
        when:
        List list = Org.dao.list([criteria:[id: ["\$nin": [2, 3, 4, 5]]], max: 150])
        then:
        list.size() == Org.createCriteria().list(){not{ inList "id", [2L, 3L, 4L, 5L]}}.size()
    }


    def "Filter with `not in()` with ids in array"(){
        when:
        List list = Org.dao.list([criteria:[id: ["\$nin": [2, 3, 4, 5]]], max: 150])
        then:
        list.size() == Org.createCriteria().list(){not{ inList "id", [2L, 3L, 4L, 5L]}}.size()
    }

    def "test paging, defaults"(){
        when:
        List list = Org.dao.list([:])
        then:
        list.size() == 10
    }
    def "test paging"(){
        when:
        List list = Org.dao.list([max:20])
        then:
        list.size() == 20
    }

    def "test closure"(){
        when:
        List list = Org.dao.list([max:20]){
            or {
                eq "id", 2L
            }
        }
        then:
        list.size() == 1
    }

    def "test closure with params"(){
        when:
        List list = Org.dao.list([criteria:[id: ["\$in":[ 24, 25, 18, 19]]], max: 150]) {
            gt "id", 19L
        }

        then:
        list.size() == 2
        list[0].name == "Org#23"
        list[1].name == "Org#24"

    }

    def "test quick search"(){
        when:
        List list = Org.dao.list([criteria:['$quickSearch': "Org#2%"], max: 150])
        then:
        list.size() == 11

    }

    def "test count totals"(){
        when:
        Map totals = Org.dao.countTotals([:],['credit', 'id'])
        then:
        totals.id == Org.list().sum{it.id}
        totals.credit == Org.list().sum{it.credit}

        when:
        totals = Org.dao.countTotals([criteria:[id:4]],['credit', 'id'])
        then:
        totals.id == 4
        totals.credit == 5000
    }

}
