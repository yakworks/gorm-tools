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
        Org.dao.search().size() == 10
    }

    def "Filter by Name eq"() {
        when:
        List list = Org.dao.search([criteria:[name: "Org#23"],max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#23"
    }

    def "Filter by id eq"() {
        when:
        List list = Org.dao.search([criteria:[id: "24"], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#23"
    }

    def "Filter by id inList"() {
        when:
        List list = Org.dao.search([criteria:[id: ["24", "25"]], max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }

    def "Filter by Name ilike"() {
        when: "eq"
        List list = Org.dao.search([criteria:[name: "Org#2%"], max: 150])
        then:
        list.size() == 11
        list[0].name == "Org#2"
        list[1].name == "Org#20"
        list[10].name == "Org#29"
    }

    def "Filter by nested id"() {
        when: "eq"
        List list = Org.dao.search([criteria:[address: [id: 2]], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
        list[0].address.id == 2
    }

    def "Filter by nestedId"() {
        when: "eq"
        List list = Org.dao.search([criteria:[addressId: 2], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
        list[0].address.id == 2
    }

    def "Filter by nested id inList"() {
        when:
        List list = Org.dao.search([criteria:[address:[id: ["24", "25", "26"]]], max: 150])
        then:
        list.size() == 3
        list[0].name == "Org#23"
    }

    def "Filter by nested string"() {
        when: "eq"
        List list = Org.dao.search([criteria:[address: [city: "City#2"]], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#2"
        list[0].address.id == 3
    }

    def "Filter by nested string ilike"() {
        when: "eq"
        List list = Org.dao.search([criteria:[address: [city: "City#2%"]], max: 150])
        then:
        list.size() == 11
        list[0].name == "Org#2"
        list[1].name == "Org#20"
        list[10].name == "Org#29"
        list[0].address.id == 3
        list[1].address.id == 21
        list[10].address.id == 30
    }

    /*def "Filter by boolean"() {
        when:
        List list = Org.dao.search([[criteria:isActive: "true"], max: 150])
        then:
        list.size() == 51
    }*/

    def "Filter by boolean in list"() {
        when:
        List list = Org.dao.search([criteria:[isActive: ["false"]], max: 150])
        then:
        list.size() == 50
        list[0].isActive == false
        list[1].isActive == false
    }

    def "Filter by BigDecimal"() {
        when:
        List list = Org.dao.search([criteria:[revenue: "200"], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#2"
    }

    def "Filter by BigDecimal in list"() {
        when:
        List list = Org.dao.search([criteria:[revenue: ["200", "500"]], max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#2"
        list[1].name == "Org#5"
    }

    def "Filter by Date"() {
        when:
        List list = Org.dao.search([criteria:[testDate: (new Date() +1).clearTime()], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }

    def "Filter by xxxId 1"() {
        when: "xxxId in domain"
        List list = Org.dao.search([criteria:[refId: "200"], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }
    def "Filter by xxxId 2"() {

        when: "xxxId in nested domain"
        List list = Org.dao.search([criteria:["address.testId": "9"], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#3"
    }


    def "Filter by xxxId 3"(){
        when: "xxxId in nested domain2"
        List list = Org.dao.search([criteria:[address:[testId: "3"]], max: 150])
        then:
        list.size() == 1
        list[0].name == "Org#1"
    }

    def "Filter by xxxId 4"() {
        when: "xxxId in nested domain"
        List list = Org.dao.search([criteria:["address.testId": ["9", "12"]], max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#3"
    }

    def "Filter by xxxId 4 criteria as String"() {
        when:
        Map params = [max: 150]
        params.criteria = (["address.testId": ["9", "12"]] as grails.converters.JSON).toString()
        List list = Org.dao.search(params)
        then:
        list.size() == 2
        list[0].name == "Org#3"
    }


    def "Filter with `or`"(){
        when:
        List list = Org.dao.search([criteria:[or: ["name": "Org#1", "address.id": "4" ]], max: 150])
        then:
        list.size() == 2
        list[0].name == "Org#1"
        list[1].name == "Org#3"
    }

    def "Filter with `or` with like"(){
        when:
        List list = Org.dao.search([criteria:[or: ["name": "Org#2%", "address.id": "4" ]], max: 150]).sort{it.id}
        then:
        list.size() == 12
        list[0].name == "Org#2"
        list[1].name == "Org#3"
        list[2].name == "Org#20"
    }

    def "Filter with `between()`"(){
        when:
        List list = Org.dao.search([criteria:[id: ["between()", 2, 10]], max: 150]).sort{it.id}
        then:
        list.size() == 9
        list[0].name == "Org#1"
        list[1].name == "Org#2"
        list[-1].name == "Org#9"
    }

    def "Filter with `in()`"(){
        when:
        List list = Org.dao.search([criteria:[id: ["in()", "24", "25"]], max: 150]).sort{it.id}
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }
    def "Filter with `inList()`"(){
        when:
        List list = Org.dao.search([criteria:[id: ["inList()", "24", "25"]], max: 150]).sort{it.id}
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }
    def "Filter by Name ilike()"() {
        when:
        List list = Org.dao.search([criteria:[name:["ilike()", "Org#2%"]], max: 150])
        then:
        list.size() == 11
        list[0].name == "Org#2"
        list[1].name == "Org#20"
        list[10].name == "Org#29"
    }

    def "Filter with `gt()`"(){
        when:
        List list = Org.dao.search([criteria:[id: ["gt()", "95"]], max: 150]).sort{it.id}
        then:
        list.size() == 7
        list[0].name == "Org#95"
    }

    def "Filter with `gte()`"(){
        when:
        List list = Org.dao.search([criteria:[id: ["ge()", "95"]], max: 150]).sort{it.id}
        then:
        list.size() == 8
        list[0].name == "Org#94"
    }

    def "Filter with `lt()`"(){
        when:
        List list = Org.dao.search([criteria:[id: ["lt()", "5"]], max: 150]).sort{it.id}
        then:
        list.size() == 3
        list[0].name == "Org#1"
    }

    def "Filter with `lte()`"(){
        when:
        List list = Org.dao.search([criteria:[id: ["le()", "5"]], max: 150]).sort{it.id}
        then:
        list.size() == 4
        list[0].name == "Org#1"
    }


    def "Filter with `not in()`"(){
        when:
        List list = Org.dao.search([criteria:[id: ["not in()", 2, 3, 4, 5]], max: 150])
        then:
        list.size() == 97
    }


    def "Filter with `not in()` with ids in array"(){
        when:
        List list = Org.dao.search([criteria:[id: ["not in()", [2, 3, 4, 5]]], max: 150])
        then:
        list.size() == 97
    }

    def "test paging, defaults"(){
        when:
        List list = Org.dao.search([:])
        then:
        list.size() == 10
    }
    def "test paging"(){
        when:
        List list = Org.dao.search([max:20])
        then:
        list.size() == 20
    }

    def "test closure"(){
        when:
        List list = Org.dao.search([max:20]){
            or {
                eq "id", 2L
            }
        }
        then:
        list.size() == 1
    }

    def "test closure with params"(){
        when:
        List list = Org.dao.search([criteria:[id: ["in()", "24", "25", "18", "19"]], max: 150]) {
            gt "id", 19L
        }
        then:
        list.size() == 2
        list[0].name == "Org#23"
        list[1].name == "Org#24"

    }

    def "test quick search"(){
        when:
        List list = Org.dao.search([criteria:[quickSearch: "Or"], max: 150])
        then:
        list.size() == 101

    }

    def "test quick search is higher priority then other filters"(){
        when:
        List list = Org.dao.search([criteria:[quickSearch: "Cit", id: 123], max: 150])
        then:
        list.size() == 101

    }

}
