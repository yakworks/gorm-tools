package yakworks

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.taskify.domain.Org

@Integration
@Rollback
class MangoSpec extends Specification {

    def "Check list"() {
        expect:
        Org.queryList().size() == 10
    }

    // @IgnoreRest
    def "Filter by Name eq"() {
        when:
        List list = Org.query([name: "Org23"]).list()
        then:
        list.size() == 1
        list[0].name == "Org23"
        list[0].id == 23
    }

    // @IgnoreRest
    def "Filter by id eq"() {
        when:
        List list = Org.query(id: 24).list()
        then:
        list.size() == 1
        list[0].name == "Org24"
    }

    def "Filter by id inList"() {
        when:
        List list = Org.query(id: [24, 25]).list()
        then:
        list.size() == 2
        list[0].name == "Org24"
    }

    def "Filter by Name ilike"() {
        when: "eq"
        List list = Org.queryList([criteria: [name: "Org2%"], max: 150])
        then:
        list.size() == 11
        list[0].name == "Org2"
        list[1].name == "Org20"
        list[10].name == "Org29"
    }

    def "Filter by nested id"() {
        when: "eq"
        List list = Org.repo.queryList([criteria: [location: [id: 2]]])
        then:
        list.size() == 1
        list[0].name == "Org2"
        list[0].location.id == 2
    }

    def "Filter by nested.id"() {
        when: "eq"
        List list = Org.repo.queryList([criteria: ["location.id": 2]])
        then:
        list.size() == 1
        list[0].name == "Org2"
        list[0].location.id == 2
    }

    def "Filter by nested id inList"() {
        when:
        List list = Org.repo.queryList([criteria: [location: [id: [24, 25, 26]]]])
        then:
        list.size() == 3
        list[0].name == "Org24" //sanity check
    }

    def "Filter by nested string"() {
        when: "eq"
        List list = Org.query(location: [city: "City2"]).list()
        then:
        list.size() == 1
        list[0].name == "Org2"
        list[0].location.id == 2
    }

    def "Filter by nested string ext"() {
        when: "eq"
        List list = Org.queryList([criteria: [ext: [text1: "Ext4"]]])
        then:
        list.size() == 1
        list[0].name == "Org4"
        list[0].ext.text1 == "Ext4"
        list[0].ext.orgParentId == 1
    }

    def "Filter by nested string ilike"() {
        when: "eq"
        List list = Org.repo.query(location: [city: "City2%"]).list(max: 50)
        then:
        list.size() == 11
        list[0].location.city == "City2"
        list[1].location.city == "City20"
        list[10].location.city == "City29"
    }

    def "Filter by boolean"() {
        when:
        List list = Org.repo.query(inactive: true).list()
        then:
        list.size() == Org.createCriteria().list() { eq "inactive", true }.size()
    }

    def "Filter by boolean in list"() {
        when:
        List list = Org.repo.queryList([inactive: [false], max: 100])
        then:
        list.size() == 50
        list[0].inactive == false
        list[1].inactive == false
    }

    def "Filter by BigDecimal"() {
        when:
        List list = Org.repo.query(revenue: 2.50).list()
        then:
        list.size() == 1
        list[0].revenue == 2.50
        list[0].name == "Org3"
    }

    def "Filter by BigDecimal in list"() {
        when:
        List list = Org.query(revenue: [2.50, 3.75]).list()
        then:
        list.size() == 2
        list[0].revenue == 2.50
        list[0].name == "Org3"
        list[1].revenue == 3.75
        list[1].name == "Org4"
    }

    def "Filter by Date"() {
        when:
        List list = Org.repo.queryList([criteria: [actDate: (new Date() + 1).clearTime()]])
        then:
        list.size() == 1
        list[0].name == "Org1"
    }

    def "Filter by Date le"() {
        when:
        List list = Org.repo.queryList([criteria: ['actDate.$lte': (new Date() + 1).clearTime()], max: 150])
        then:
        list.size() == Org.createCriteria().list() { le "actDate", (new Date() + 1).clearTime() }.size()
        list[0].name == Org.createCriteria().list() { le "actDate", (new Date() + 1).clearTime() }[0].name
    }

    def "Filter by xxxId 1"() {
        when: "xxxId in domain"
        List list = Org.repo.queryList([criteria: [locationId: 2]])
        then:
        list.size() == 1
        list[0].locationId == 2
        list[0].location.id == 2
        list[0].name == "Org2"
    }

    def "Filter by ext.parentOrg.id"() {

        when: "xxxId in nested domain"
        List list = Org.repo.queryList([criteria: ["ext.orgParentId": 1], max: 150])
        then:
        list.size() == 49
    }


    def "Filter by xxxId 3"() {
        when: "xxxId in nested domain2"
        List list = Org.repo.queryList([criteria: [ext: [orgParentId: 2]], max: 150])
        then:
        list.size() == 49
    }

    def "Filter by xxxId 4"() {
        when: "xxxId in nested domain"
        List list = Org.repo.queryList([criteria: ["ext.orgParentId": [1, 2]], max: 150])
        then:
        list.size() == 98 // all but first 2
    }

    @Ignore
    def "Filter by xxxId 4 criteria as JSON"() {
        when:
        Map params = [max: 150]
        params.criteria = (["address.testId": [9, 12]] as grails.converters.JSON).toString()
        List list = Org.repo.queryList(params)
        then:
        list.size() == 2
        list[0].name == "Org#3"
    }


    def "Filter with `or` "() {
        when:
        List list = Org.repo.queryList([criteria: ['$or': ["name": "Org1", "location.id": 4]]])
        then:
        list.size() == 2
        list[0].name == "Org1"
        list[1].name == "Org4"
    }

    def "Filter with `or` on low level"() {
        when:
        List list = Org.repo.queryList([criteria: [location: ['$or': ["city": "City1", "id": 4]]], max: 150])
        then:
        list.size() == 2
        list[0].name == "Org1"
        list[1].name == "Org4"
    }

    def "Filter with several `or` on one level"() {
        when:
        List list = Org.repo.queryList([criteria: ['$or': [
            ["location.id": 5],
            ["name": "Org4", "location.city": "City4"]
        ]]])

        then:
        list.size() == 2
    }

    def "Filter with several `or` on one level2"() {
        when:
        List list = Org.repo.queryList([criteria: ['$or': [["location.id": 5], ["location.id": 4]]]])
        then:
        list.size() == 2
    }

    def "Filter with `or` with like"() {
        when:
        List list = Org.repo.queryList([criteria: ['$or': ["name": "Org2%", "location.id": 4]], max: 50])
        then:
        list.size() == 12
    }

    def "Filter with `between()`"() {
        when:
        List list = Org.repo.queryList([criteria: [id: ['$between': [2, 10]]]])
        then:
        list.size() == 9
    }

    def "Filter with `in()`"() {
        when:
        List list = Org.repo.queryList([criteria: [id: ["\$in": [24, 25]]]])
        then:
        list.size() == 2
    }

    def "Filter with `inList()`"() {
        when:
        List list = Org.repo.queryList([criteria: [id: ["\$inList": [24, 25]]]])
        then:
        list.size() == 2
    }

    def "Filter by Name ilike()"() {
        when:
        List list = Org.repo.queryList([criteria: [name: ['$ilike': "Org2%"]], max: 50])
        then:
        list.size() == 11
    }

    def "Filter with `gt()`"() {
        when:
        List list = Org.repo.queryList([criteria: [id: ['$gt': 95]]])
        then:
        list.size() == 5
    }

    def "Filter with `ge()`"() {
        when:
        List list = Org.repo.queryList([criteria: [id: ['$gte': 95]]])
        then:
        list.size() == 6
    }

    def "Filter with `ge()` for bigdecimal"() {
        when:
        List list = Org.repo.queryList([criteria: [revenue: ['$gte': 100.0]], max: 150])
        then:
        list.size() == Org.createCriteria().list() { ge "revenue", 100.0 }.size()
    }

    def "Filter with `lte()` then another value"() {
        when:
        List list = Org.repo.queryList([criteria: [revenue: ['$ltef': "creditLimit"]], max: 150])
        then:
        list.size() == Org.createCriteria().list() { leProperty "revenue", "creditLimit" }.size()
    }

    def "Filter with `gte()` then another value"() {
        when:
        List list = Org.repo.queryList([criteria: [revenue: ["\$gtef": "creditLimit"]], max: 150])
        then:
        list.size() == Org.createCriteria().list() { geProperty "revenue", "creditLimit" }.size()
    }

    def "Filter with `gt()` then another value"() {
        when:
        List list = Org.repo.queryList([criteria: [revenue: ["\$gtf": "creditLimit"]], max: 150])
        then:
        list.size() == Org.createCriteria().list() { gtProperty "revenue", "creditLimit" }.size()
    }

    def "Filter with `lt()` then another value"() {
        when:
        List list = Org.repo.queryList([criteria: [revenue: ["\$ltf": "creditLimit"]], max: 150]).sort { it.id }
        then:
        list.size() == Org.createCriteria().list() { ltProperty "revenue", "creditLimit" }.size()
    }

    def "Filter with `lt()`"() {
        when:
        List list = Org.repo.queryList([criteria: [id: ["\$lt": 5]], max: 150]).sort { it.id }
        then:
        list.size() == Org.createCriteria().list() { lt "id", 5L }.size()
        list[0].name == Org.createCriteria().list() { lt "id", 5L }[0].name
    }

    def "Filter with `le()`"() {
        when:
        List list = Org.repo.queryList([criteria: [id: ["\$lte": 5]], max: 150]).sort { it.id }
        then:
        list.size() == Org.createCriteria().list() { le "id", 5L }.size()
        list[0].name == Org.createCriteria().list() { le "id", 5L }[0].name
    }

    def "Filter with `isNull` object"() {
        when:
        List list = Org.repo.queryList([criteria: [name2: ["\$isNull": true]], max: 150]).sort { it.id }
        then:
        list.size() == Org.createCriteria().list() { isNull "name2" }.size()
    }

    def "Filter with `isNull` when val"() {
        when:
        List list = Org.repo.queryList([criteria: [name2: '$isNull'], max: 100]).sort { it.id }
        then:
        list.size() == 50
    }

    def "Filter with `isNull` when just null"() {
        when:
        List list = Org.repo.queryList([criteria: [name2: null], max: 100])
        then:
        list.size() == Org.createCriteria().list() { isNull "name2" }.size()
    }


    def "Filter with `not in()`"() {
        when:
        List list = Org.repo.queryList([criteria: [id: ["\$nin": [2, 3, 4, 5]]], max: 150])
        then:
        list.size() == Org.createCriteria().list() { not { inList "id", [2L, 3L, 4L, 5L] } }.size()
    }


    def "Filter with `not in()` with ids in array"() {
        when:
        List list = Org.repo.queryList([criteria: [id: ["\$nin": [2, 3, 4, 5]]], max: 150])
        then:
        list.size() == Org.createCriteria().list() { not { inList "id", [2L, 3L, 4L, 5L] } }.size()
    }

    def "Filter on enums"() {
        when:
        List list = Org.queryList([criteria: [kind: ['$in': ['CLIENT', 'VENDOR']]], max: 150])
        then:
        list.size() == 98
    }

    def "Filter on identity enum"() {
        when:
        // 3 does ot exists, should show 50 as half have 1
        List list = Org.queryList([criteria: [status: ['$in': [1, 3]]], max: 150])

        then:
        list.size() == 50
    }

    def "test paging, defaults"() {
        when:
        List list = Org.queryList([:])
        then:
        list.size() == 10
    }

    def "test paging"() {
        when:
        List list = Org.queryList([max: 20])
        then:
        list.size() == 20
    }

    def "test closure"() {
        when:
        List list = Org.repo.queryList([max: 20]) {
            or {
                eq "id", 2L
            }
        }
        then:
        list.size() == 1
    }

    def "test closure with params"() {
        when:
        List list = Org.repo.queryList([criteria: [id: ['$in': [24, 25, 18, 19]]], max: 150]) {
            gt "id", 19L
        }

        then:
        list.size() == 2

    }

    def "test quick search"() {
        when:
        List list = Org.repo.queryList([criteria: ['$q': "Org2"], max: 150])
        then:
        list.size() == 11

    }

}
