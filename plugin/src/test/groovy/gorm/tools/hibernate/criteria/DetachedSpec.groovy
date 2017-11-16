package gorm.tools.hibernate.criteria

import grails.persistence.Entity
import grails.test.mixin.TestMixin
import grails.test.mixin.gorm.Domain
import grails.test.mixin.hibernate.HibernateTestMixin
import spock.lang.Specification

@Domain([Org])
@TestMixin(HibernateTestMixin)
class DetachedSpec extends Specification {

    def setup() {
        (1..10).each { index ->
            String value = "Name#" + index
            new Org(id: index,
                     name: value,
                     isActive: index % 2
            ).save()
        }
    }

    def "test detached isActive"() {
        setup:
        (1..10).each { index ->
            String value = "Name#" + index
            new Org(id: index,
                    name: value,
                    isActive: (index % 2 == 0 )
            ).save(failOnError: true)
        }
        when:
        DynamicCriteriaBuilder dcb = new DynamicCriteriaBuilder(Org)
        List res = dcb.list([isActive:true])

        then:
        res.size() == 5
    }

    def "test detached combined"() {
        when:
        DynamicCriteriaBuilder dcb = new DynamicCriteriaBuilder(Org)
        List res = dcb.list([isActive:true, name: "Name#1"])

        then:
        res.size() == 1
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

    static constraints = {
        name blank: true, nullable: true
        isActive nullable: true
    }
}

