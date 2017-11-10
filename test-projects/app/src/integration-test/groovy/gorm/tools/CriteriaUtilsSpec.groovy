package gorm.tools

import spock.lang.Specification
import testing.Jumper
import daoapp.Org
import gorm.tools.hibernate.criteria.CriteriaUtils

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class CriteriaUtilsSpec extends Specification {

    def "Check list"() {
        expect:
        Org.list().size() == CriteriaUtils.list([:], Org, [max: 150]).size()
    }

    def "Filter by Name eq"() {
        when:
        List list = CriteriaUtils.list([name: "Org#23"], Org, [max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#23"
    }

    def "Filter by id eq"() {
        when:
        List list = CriteriaUtils.list([id: "24"], Org, [max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#23"
    }

    def "Filter by id inList"() {
        when:
        List list = CriteriaUtils.list([id: ["24", "25"]], Org, [max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }

    def "Filter by Name ilike"() {
        when: "eq"
        List list = CriteriaUtils.list([name: "Org#2%"], Org, [max: 150])
        then:
        list.size() == 11
        list[0].name == "Org#2"
        list[1].name == "Org#20"
        list[10].name == "Org#29"
    }

    def "Filter by nested id"() {
        when: "eq"
        List list = CriteriaUtils.list([address: [id: 2]], Org, [max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
        list[0].address.id == 2
    }

    def "Filter by nestedId"() {
        when: "eq"
        List list = CriteriaUtils.list([addressId: 2], Org, [max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
        list[0].address.id == 2
    }

    def "Filter by nested id inList"() {
        when:
        List list = CriteriaUtils.list([address:[id: ["24", "25", "26"]]], Org, [max: 150])
        then:
        list.size() == 3
        list[0].name == "Org#23"
    }

    def "Filter by nested string"() {
        when: "eq"
        List list = CriteriaUtils.list([address: [city: "City#2"]], Org, [max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#2"
        list[0].address.id == 3
    }

    def "Filter by nested string ilike"() {
        when: "eq"
        List list = CriteriaUtils.list([address: [city: "City#2%"]], Org, [max: 150])
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
        List list = CriteriaUtils.list([isActive: "true"], Org, [max: 150])
        then:
        list.size() == 51
    }

    def "Filter by boolean in list"() {
        when:
        List list = CriteriaUtils.list([isActive: ["false"]], Org, [max: 150])
        then:
        list.size() == 50
        list[0].isActive == false
        list[1].isActive == false
    }

    def "Filter by BigDecimal"() {
        when:
        List list = CriteriaUtils.list([revenue: "200"], Org, [max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#2"
    }

    def "Filter by BigDecimal in list"() {
        when:
        List list = CriteriaUtils.list([revenue: ["200", "500"]], Org, [max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#2"
        list[1].name == "Org#5"
    }

    def "Filter by Date"() {
        when:
        List list = CriteriaUtils.list([testDate: (new Date() +1).clearTime()], Org, [max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }

    def "Filter by xxxId 1"() {
        when: "xxxId in domain"
        List list = CriteriaUtils.list([refId: "200"], Org, [max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }
    def "Filter by xxxId 2"() {

        when: "xxxId in nested domain"
        List list = CriteriaUtils.list(["address.testId": "9"], Org, [max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#3"
    }


    def "Filter by xxxId 3"(){
        when: "xxxId in nested domain2"
        List list = CriteriaUtils.list([address:[testId: "3"]], Org, [max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }

    def "Filter by xxxId 4"() {

        when: "xxxId in nested domain"
        List list = CriteriaUtils.list(["address.testId": ["9", "12"]], Org, [max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#3"
    }


    def "Filter with `or`"(){
        when:
        List list = CriteriaUtils.list(["\$or": ["name": "Org#1", "address.id": "4" ]], Org, [max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#1"
        list[1].name == "Org#3"
    }

    def "Filter with `or` with like"(){
        when:
        List list = CriteriaUtils.list(["\$or": ["name": "Org#2%", "address.id": "4" ]], Org, [max: 150]).sort{it.id}
        then:
        list.size() == 12
        list[0].name == "Org#2"
        list[1].name == "Org#3"
        list[2].name == "Org#20"
    }

    def "Filter with `between()`"(){
        when:
        List list = CriteriaUtils.list([id: ["\$between", 2, 10]], Org, [max: 150]).sort{it.id}
        then:
        list.size() == 9
        list[0].name == "Org#1"
        list[1].name == "Org#2"
        list[-1].name == "Org#9"
    }

    def "Filter with `in()`"(){
        when:
        List list = CriteriaUtils.list([id: ["\$in", "24", "25"]], Org, [max: 150]).sort{it.id}
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }
    def "Filter with `inList()`"(){
        when:
        List list = CriteriaUtils.list([id: ["\$inList", "24", "25"]], Org, [max: 150]).sort{it.id}
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }
    def "Filter by Name ilike()"() {
        when: "eq"
        List list = CriteriaUtils.list([name:["\$ilike", "Org#2%"]], Org, [max: 150])
        then:
        list.size() == 11
        list[0].name == "Org#2"
        list[1].name == "Org#20"
        list[10].name == "Org#29"
    }
    def "Filter with `gt()`"(){
        when:
        List list = CriteriaUtils.list([id: ["\$gt", "95"]], Org, [max: 150]).sort{it.id}
        then:
        list.size() == 7
        list[0].name == "Org#95"
    }
    def "Filter with `gte()`"(){
        when:
        List list = CriteriaUtils.list([id: ["\$ge", "95"]], Org, [max: 150]).sort{it.id}
        then:
        list.size() == 8
        list[0].name == "Org#94"
    }

    def "Filter with `lt()`"(){
        when:
        List list = CriteriaUtils.list([id: ["\$lt", "5"]], Org, [max: 150]).sort{it.id}
        then:
        list.size() == 3
        list[0].name == "Org#1"
    }

    def "Filter with `lte()`"(){
        when:
        List list = CriteriaUtils.list([id: ["\$le", "5"]], Org, [max: 150]).sort{it.id}
        then:
        list.size() == 4
        list[0].name == "Org#1"
    }

    def "Filter with `ne()`"(){
        when:
        List list = CriteriaUtils.list([id: ["\$ne", "5"]], Org, [max: 150]).sort{it.id}
        then:
        list.size() == Org.list().size() -1
    }
    def "Filter with `not in()`"(){
        when:
        List list = CriteriaUtils.list([id: ["\$nin", 2, 3, 4, 5]], Org, [max: 150]).sort{it.id}
        then:
        list.size() == Org.list().size() - 4
    }
    def "Filter with `not in()` with ids in array"(){
        when:
        List list = CriteriaUtils.list([id: ["\$nin", [2, 3, 4, 5]]], Org, [max: 150]).sort{it.id}
        then:
        list.size() == Org.list().size() - 4
    }



    def "test paging, defaults"(){
        when:
        List list = CriteriaUtils.list([:], Org)
        then:
        list.size() == 10
    }
    def "test paging"(){
        when:
        List list = CriteriaUtils.list([:], Org, [max:20])
        then:
        list.size() == 20
    }

    def "test map flatten"(){
        when:
        Map map = CriteriaUtils.flattenMap([dispute:[reason:[id: 1]]])
        then:
        map == ["dispute.reason.id": 1]
    }
}
