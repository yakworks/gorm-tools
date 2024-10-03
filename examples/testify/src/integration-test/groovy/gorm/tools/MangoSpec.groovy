package gorm.tools

import java.time.LocalDate

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex

@Integration
@Rollback
class MangoSpec extends Specification {

    def "sanity Check list"() {
        expect:
        Org.repo.query([:]).pagedList().size() == 20
    }

    def "Filter by Name eq"() {
        when:
        List list = Org.query([name: "Org23"]).list()
        then:
        list.size() == 1
        list[0].name == "Org23"
        list[0].id == 23
    }

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

    void "Filter by xxxId for criteria as JSON"() {
        when:
        Map params = [:]
        params.criteria = '{"id":[9, 12]}'
        List list = Org.repo.query(params).list()
        then:
        list.size() == 2
        list[0].num == "9"
        list[1].num == "12"
    }

    def "Filter by Name ilike"() {
        when: "eq"
        List list = Org.repo.query([q: [name: "Org2%"], max: 150]).pagedList()
        then:
        list.size() == 10
        list[0].name == "Org20"
        list[9].name == "Org29"
    }

    def "Filter by Name wildcard"() {
        when: "eq"
        List list = Org.repo.query(name: "Org2*").list()
        then:
        list.size() == 10
        list[0].name == "Org20"
        list[9].name == "Org29"
    }

    def "Filter by nested id"() {
        when: "eq"
        List list = Org.repo.query(q: [flex: [id: 2]]).list()
        then:
        list.size() == 1
        list[0].num == "2"
        list[0].flex.id == 2
    }

    def "Filter by nested.id"() {
        when: "eq"
        List list = Org.repo.query(q: ["flex.id": 2]).list()
        then:
        list.size() == 1
        list[0].num == "2"
        list[0].flex.id == 2
    }

    def "Filter by nested id inList"() {
        when:
        List list = Org.repo.query([q: [flex: [id: [24, 25, 26]]]]).list()
        then:
        list.size() == 3
        list[0].num == "24" //sanity check
    }

    def "Filter by nested string"() {
        when: "eq"
        List list = Org.query(info: [phone: "1-800-4"]).list()
        then:
        list.size() == 1
    }

    def "Filter by nested string on info"() {
        when: "eq"
        List list = Org.repo.query('info.phone': "1-800-4").list()
        then:
        list.size() == 1
        list[0].num == "4"
        list[0].info.phone == "1-800-4"
    }

    def "Filter by nested string ilike"() {
        when: "eq"
        List list = Org.repo.query('location.city': "City2%").list(max: 50)
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
        List list = Org.repo.query([inactive: [false]]).list()
        then:
        list.size() == 50
        list[0].inactive == false
        list[1].inactive == false
    }

    def "Filter by BigDecimal"() {
        when:
        List list = Org.repo.query(flex:[num1: 2.50]).list()
        then:
        list.size() == 1
        list[0].flex.num1 == 2.50
        list[0].num == "3"
    }

    def "Filter by BigDecimal in list"() {
        when:
        List list = Org.query(flex: [num1: [2.50, 3.75]]).list()
        then:
        list.size() == 2
        list[0].flex.num1 == 2.50
        list[0].num == "3"
        list[1].flex.num1 == 3.75
        list[1].num == "4"
    }

    def "Filter by BigDecimal in list dot"() {
        when:
        List list = Org.query('flex.num1': [2.50, 3.75]).list()
        then:
        list.size() == 2
        list[0].flex.num1 == 2.50
        list[0].num == "3"
        list[1].flex.num1 == 3.75
        list[1].num == "4"
    }

    def "Filter by LocDate"() {
        when:
        List list = Org.repo.query(flex: [date1: LocalDate.now().plusDays(1).atStartOfDay()]).list()
        then:
        list.size() == 1
        list[0].name == "Org1"
    }

    def "Filter by Date le"() {
        when:
        List list = Org.repo.query(flex: ['date1.$lte': LocalDate.now().plusDays(3).atStartOfDay() ]).list()
        then:
        list.size() == 3
    }

    def "Filter by xxxId 1"() {
        when: "xxxId in domain"
        List list = Org.repo.query(flexId: 2).list()
        then:
        list.size() == 1
        list[0].flexId == 2
    }

    def "Filter with `or`"() {
        when:
        List list = Org.repo.query('$or': ["num": "9", "flexId": 10]).list()
        then:
        list.size() == 2
        list[0].num == "9"
        list[1].num == "10"
    }

    def "Filter with `or` dot id"() {
        when:
        List list = Org.repo.query('$or': ["num": "9", "flex.id": 10]).list()
        then:
        list.size() == 2
        list[0].num == "9"
        list[1].num == "10"
    }

    def "Filter with `or` on low level"() {
        when:
        def crit = Org.query(q: [location: ['$or': ["city": "City3", "id": 1000]]])
        //crit.select('location')
        List list = crit.list(max: 150)
        then:
        //should only be 1, id 100 does not exist
        list.size() == 1
    }

    def "Filter with several `or` on one level"() {
        when:
        def crit = Org.query(
            '$or': [
                ["location.city": "City3"],
                ["num": "4", "location.city": "City4"]
            ],
            sort: "contact.location.city"
        )
        List list = crit.list()
        then:
        list.size() == 2
    }

    def "location alias on org and on contact works"() {
        //this was broken since Org has location alias and so does contact on the sort
        when:
        def crit = Org.query(
            "location.city":["City10"],
            sort: "contact.location.city"
        )
        List list = crit.list()
        then:
        list.size() == 1
    }

    def "Filter with several `or` on same level as root using id"() {
        when:
        List list = Org.query('$or': [["contact.id": 10], ["contact.id": 11], ["location.id": -999]]).list()
        then:
        //should only be 2, -999 does not exist
        list.size() == 2
    }

    def "Filter with `or` with like"() {
        when:
        List list = Org.query('$or': ["num": "2%", "location.city": "City4"]).list()
        then:
        list.size() == 12
    }

    def "Filter with `between()`"() {
        when:
        List list = Org.query(id: ['$between': [2, 10]]).list()
        then:
        list.size() == 9
    }

    def "Filter with `in()`"() {
        when:
        List list = Org.query(id: ['$in': [24, 25]]).list()
        then:
        list.size() == 2
    }

    def "Filter with `inList()`"() {
        when:
        List list = Org.query(id: ['$inList': [24, 25]]).list()
        then:
        list.size() == 2
    }

    def "Filter by Name ilike explicit"() {
        when:
        List list = Org.query(name: ['$ilike': "Org2%"]).list()
        then:
        list.size() == 10
    }

    def "Filter with `gt()`"() {
        when:
        List list = Org.query([q: [id: ['$gt': 95]]]).list()
        then:
        list.size() == 5
    }

    def "Filter with `ge()`"() {
        when:
        List list = Org.query([q: [id: ['$gte': 95]]]).list()
        then:
        list.size() == 6
    }

    def "Filter with `ge()` for bigdecimal"() {
        when:
        List list = Org.query(flex: [num1: ['$gte': 100.0]]).list()
        then:
        list.size() == Org.createCriteria().list() { flex { ge "num1", 100.0 } }.size()
    }

    def "Filter with `lte()` then another value"() {
        when:
        List list = OrgFlex.repo.query(q: ['num1': ['$ltef': "num2"]]).list(max: 150)
        then:
        list.size() == OrgFlex.createCriteria().list() { leProperty "num1", "num2" }.size()
    }

    def "Filter with `gtef()` then another value"() {
        when:
        List list = OrgFlex.repo.query(q: [num1: ['$gtef': "num2"]]).list(max: 150)
        then:
        list.size() == OrgFlex.createCriteria().list() { geProperty "num1", "num2" }.size()
    }

    def "Filter with `gtf()` then another value"() {
        when:
        List list = OrgFlex.repo.query(q: ['num1': ['$gtf': "num2"]]).list(max: 150)
        then:
        list.size() == Org.createCriteria().list() { flex { gtProperty "num1", "num2" } }.size()
    }

    def "Filter with `ltf()` then another value"() {
        when:
        List list = OrgFlex.repo.query(q: [num1: ['$ltf': "num2"]]).list(max: 150)
        then:
        list.size() == OrgFlex.createCriteria().list() { ltProperty "num1", "num2" }.size()
    }

    def "Filter with `lt()`"() {
        when:
        List list = Org.repo.query([q: [id: ['$lt': 5]]]).list(max: 150).sort { it.id }
        then:
        list.size() == Org.createCriteria().list() { lt "id", 5L }.size()
        list[0].name == Org.createCriteria().list() { lt "id", 5L }[0].name
    }

    def "Filter with `le()`"() {
        when:
        List list = Org.repo.query([q: [id: ['$lte': 5]]]).list(max: 150).sort { it.id }
        then:
        list.size() == Org.createCriteria().list() { le "id", 5L }.size()
        list[0].name == Org.createCriteria().list() { le "id", 5L }[0].name
    }

    def "Filter with `isNull` object"() {
        when:
        List list = Org.repo.query([q: [comments: ['$isNull': true]]]).list(max: 150).sort { it.id }
        then:
        list.size() == Org.createCriteria().list() { isNull "comments" }.size()
    }

    def "Filter with `isNull` when val"() {
        when:
        List list = Org.repo.query(q: [comments: '$isNull']).list(max: 100)
        then:
        list.size() == 50
    }

    def "Filter with `isNull` when just null"() {
        when:
        List list = Org.repo.query([q: [comments: null]]).list(max: 100)
        then:
        list.size() == Org.createCriteria().list() { isNull "comments" }.size()
    }


    def "Filter with `not in()`"() {
        when:
        List list = Org.repo.query([q: [id: ["\$nin": [2, 3, 4, 5]]]]).list(max: 150)
        then:
        list.size() == Org.createCriteria().list() { not { inList "id", [2L, 3L, 4L, 5L] } }.size()
    }


    def "Filter with `not in()` with ids in array"() {
        when:
        List list = Org.repo.query([q: [id: ['$nin': [2, 3, 4, 5]]]]).list(max: 150)
        then:
        list.size() == Org.executeQuery("from Org where id not in (2, 3, 4, 5)").size()
    }

    def "Filter on enums"() {
        when:
        List list = Org.repo.query(location: [
            kind: [
                '$in': ['remittance', 'other']
            ]
        ]).list()
        then:
        list.size() == 2
    }

    def "Filter on identity enum"() {
        when:
        List list = Org.repo.query(type: ['$in': [5, 6]]).list()

        then:
        list.size() == 2
    }

    def "test pagedList, defaults"() {
        when:
        List list = Org.repo.query([:]).pagedList()
        then:
        list.size() == 20
    }

    def "test pagedList"() {
        when:
        List list = Org.repo.query([max: 30]).pagedList()
        then:
        list.size() == 30
    }

    def "test closure"() {
        when:
        List list = Org.repo.query([max: 20]) {
            or {
                eq "id", 2L
            }
        }.pagedList()
        then:
        list.size() == 1
    }

    def "test closure with params"() {
        when:
        List list = Org.repo.query(id: ['$in': [24, 25, 18, 19]]) {
            gt "id", 19L
        }.pagedList()

        then:
        list.size() == 2

    }

    def "test quick search"() {
        when:
        List list = Org.repo.query(q: "Org2").pagedList()
        then:
        list.size() == 10

    }

    def "test quick search with q"() {
        when:
        List list = Org.repo.query(q: "Org2").list()
        then:
        list.size() == 10

    }

}
