package gorm.tools.mango

import gorm.tools.Address
import gorm.tools.hibernate.criteria.DynamicCriteriaBuilder
import grails.persistence.Entity
import grails.test.mixin.TestMixin
import grails.test.mixin.gorm.Domain
import grails.test.mixin.hibernate.HibernateTestMixin
import spock.lang.Specification

@Domain([Org])
@TestMixin(HibernateTestMixin)
class MangoCriteriaSpec extends Specification {

    def setup() {
    }

    def "test detached isActive"() {
        setup:
        (1..10).each { index ->
            String value = "Name#" + index
            new Org(id: index,
                    name: value,
                    isActive: (index % 2 == 0 ),
                    amount: index*1.34,
                    location: (new Location(city: "City#$index").save())
            ).save(failOnError: true)
        }
        when:
        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([isActive:true])).list()

        then:
        res.size() == 5
    }

    def "test detached string"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([name: "Name#1"])).list()

        then:
        res.size() == 1
    }

    def "test detached like"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([name: "Name#%"])).list()

        then:
        res.size() == 10
    }

    def "test combined"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([amount: [1*1.34, 2*1.34, 3*1.34, 4*1.34], isActive:true])).list()

        then:
        res.size() == 2
    }

    def "test detached BigDecimal"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([amount: 1.34])).list()

        then:
        res.size() == 1
    }

    def "test gt"() {
        when:

        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy([id: ['$gt':4]])).list()

        then:
        res.size() == 6
    }

    def "test nested gt"() {
        when:
        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy(["location.id": ['$eq':6]])).list()

        then:
        res.size() == 1
    }

    def "test or"() {
        when:
        MangoCriteria dcb = new MangoCriteria(Org)
        List res = dcb.build(MangoTidyMap.tidy('$or':[[id:5], [id:2]])).list()

        then:
        res.size() == 2
    }

    List<Class> getDomainClasses() {
        return [Org]
    }
}

@Entity
class Org {
    int id
    String name
    Boolean isActive = false
    BigDecimal amount
    Location location

    static constraints = {
        name blank: true, nullable: true
        isActive nullable: true
        amount nullable: true
    }
}

@Entity
class Location{
    int id
    String city
}

