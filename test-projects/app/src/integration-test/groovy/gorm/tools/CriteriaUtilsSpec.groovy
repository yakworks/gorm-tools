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
}
