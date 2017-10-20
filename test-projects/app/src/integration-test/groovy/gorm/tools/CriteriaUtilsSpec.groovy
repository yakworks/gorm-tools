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
        Org.list().size() == CriteriaUtils.search2([:], Org).size()
    }

    def "Filter by Name eq"() {
        when:
        List list = CriteriaUtils.search2([name: "Org#23"], Org)
        then:
        list.size() == 1
        list[0].name == "Org#23"
    }

    def "Filter by id eq"() {
        when:
        List list = CriteriaUtils.search2([id: "24"], Org)
        then:
        list.size() == 1
        list[0].name == "Org#23"
    }

    def "Filter by id inList"() {
        when:
        List list = CriteriaUtils.search2([id: ["24", "25"]], Org)
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }

    def "Filter by Name ilike"() {
        when: "eq"
        List list = CriteriaUtils.search2([name: "Org#2%"], Org)
        then:
        list.size() == 11
        list[0].name == "Org#2"
        list[1].name == "Org#20"
        list[10].name == "Org#29"
    }

    def "Filter by nested id"() {
        when: "eq"
        List list = CriteriaUtils.search2([address: [id: 2]], Org)
        then:
        list.size() == 1
        list[0].name == "Org#1"
        list[0].address.id == 2
    }

    def "Filter by nestedId"() {
        when: "eq"
        List list = CriteriaUtils.search2([addressId: 2], Org)
        then:
        list.size() == 1
        list[0].name == "Org#1"
        list[0].address.id == 2
    }

    def "Filter by nested id inList"() {
        when:
        List list = CriteriaUtils.search2([address:[id: ["24", "25", "26"]]], Org)
        then:
        list.size() == 3
        list[0].name == "Org#23"
    }

    def "Filter by nested string"() {
        when: "eq"
        List list = CriteriaUtils.search2([address: [city: "City#2"]], Org)
        then:
        list.size() == 1
        list[0].name == "Org#2"
        list[0].address.id == 3
    }

    def "Filter by nested string ilike"() {
        when: "eq"
        List list = CriteriaUtils.search2([address: [city: "City#2%"]], Org)
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
        List list = CriteriaUtils.search2([isActive: "true"], Org)
        then:
        list.size() == 51
    }

    def "Filter by boolean in list"() {
        when:
        List list = CriteriaUtils.search2([isActive: ["false"]], Org)
        then:
        list.size() == 50
        list[0].isActive == false
        list[1].isActive == false
    }

    def "Filter by BigDecimal"() {
        when:
        List list = CriteriaUtils.search2([revenue: "200"], Org)
        then:
        list.size() == 1
        list[0].name == "Org#2"
    }

    def "Filter by BigDecimal in list"() {
        when:
        List list = CriteriaUtils.search2([revenue: ["200", "500"]], Org)
        then:
        list.size() == 2
        list[0].name == "Org#2"
        list[1].name == "Org#5"
    }

    def "Filter by Date"() {
        when:
        List list = CriteriaUtils.search2([testDate: (new Date() +1).clearTime()], Org)
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }

    def "Filter by xxxId 1"() {
        when: "xxxId in domain"
        List list = CriteriaUtils.search2([refId: "200"], Org)
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }
    def "Filter by xxxId 2"() {

        when: "xxxId in nested domain"
        List list = CriteriaUtils.search2(["address.testId": "9"], Org)
        then:
        list.size() == 1
        list[0].name == "Org#3"
    }


    def "Filter by xxxId 3"(){
        when: "xxxId in nested domain2"
        List list = CriteriaUtils.search2([address:[testId: "3"]], Org)
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }

    def "Filter by xxxId 4"() {

        when: "xxxId in nested domain"
        List list = CriteriaUtils.search2(["address.testId": ["9", "12"]], Org)
        then:
        list.size() == 2
        list[0].name == "Org#3"
    }


    def "Filter with `or`"(){
        when:
        List list = CriteriaUtils.search2([or: ["name": "Org#1", "address.id": "4" ]], Org)
        then:
        list.size() == 2
        list[0].name == "Org#1"
        list[1].name == "Org#3"
    }

    def "Filter with `or` with like"(){
        when:
        List list = CriteriaUtils.search2([or: ["name": "Org#2%", "address.id": "4" ]], Org).sort{it.id}
        then:
        list.size() == 12
        list[0].name == "Org#2"
        list[1].name == "Org#3"
        list[2].name == "Org#20"
    }

    def "Filter with `between()`"(){
        when:
        List list = CriteriaUtils.search2([id: ["between()", 2,10]], Org).sort{it.id}
        then:
        list.size() == 9
        list[0].name == "Org#1"
        list[1].name == "Org#2"
        list[-1].name == "Org#9"
    }

    def "Filter with `in()`"(){
        when:
        List list = CriteriaUtils.search2([id: ["in()", "24", "25"]], Org).sort{it.id}
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }
    def "Filter with `inList()`"(){
        when:
        List list = CriteriaUtils.search2([id: ["inList()", "24", "25"]], Org).sort{it.id}
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }
    def "Filter by Name ilike()"() {
        when: "eq"
        List list = CriteriaUtils.search2([name:["ilike()", "Org#2%"]], Org)
        then:
        list.size() == 11
        list[0].name == "Org#2"
        list[1].name == "Org#20"
        list[10].name == "Org#29"
    }
    def "Filter with `gt()`"(){
        when:
        List list = CriteriaUtils.search2([id: ["gt()", "95"]], Org).sort{it.id}
        then:
        list.size() == 7
        list[0].name == "Org#95"
    }
    def "Filter with `gte()`"(){
        when:
        List list = CriteriaUtils.search2([id: ["gte()", "95"]], Org).sort{it.id}
        then:
        list.size() == 8
        list[0].name == "Org#94"
    }
    def "Filter with `lt()`"(){
        when:
        List list = CriteriaUtils.search2([id: ["lt()", "5"]], Org).sort{it.id}
        then:
        list.size() == 3
        list[0].name == "Org#1"
    }
    def "Filter with `lte()`"(){
        when:
        List list = CriteriaUtils.search2([id: ["lte()", "5"]], Org).sort{it.id}
        then:
        list.size() == 4
        list[0].name == "Org#1"
    }

    def "Filter with `ne()`"(){
        when:
        List list = CriteriaUtils.search2([id: ["ne()", "5"]], Org).sort{it.id}
        then:
        list.size() == Org.list().size() -1
    }
    def "Filter with `not in()`"(){
        when:
        List list = CriteriaUtils.search2([id: ["not in()",2,3,4,5]], Org).sort{it.id}
        then:
        list.size() == Org.list().size() - 4
    }
}
