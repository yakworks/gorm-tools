package gorm.tools

import grails.test.hibernate.HibernateSpec
import grails.gorm.annotation.Entity
import grails.testing.gorm.DomainUnitTest

class GormMetaUtilsSpec extends HibernateSpec implements DomainUnitTest<Org> {

    List<Class> getDomainClasses() { [Org] }

    static doWithSpring = {

    }

    def "GetDomainClass"() {
        expect:
        GormMetaUtils.getDomainClass(Org)
    }


    def "GetDomainClass string"() {
        expect:
        GormMetaUtils.getDomainClass("gorm.tools.Org")
    }

    def "GetDomainClass instance"() {
        expect:
        def o = new Org()
        //assert o.
        GormMetaUtils.getDomainClass(o)
    }

    def "findDomainClass"() {
        expect:
        GormMetaUtils.findDomainClass("Org")
        GormMetaUtils.findDomainClass("org")
        GormMetaUtils.findDomainClass("gorm.tools.Org")
    }

    def "GetPersistentEntity name string"() {
        expect:
        GormMetaUtils.getPersistentEntity("gorm.tools.Org")
    }

    def "GetPersistentEntity instance"() {
        expect:
        GormMetaUtils.getPersistentEntity(new Org())
    }

    def "GetPersistentEntity"() {
        expect:
        GormMetaUtils.getPersistentEntity(Org)
    }

    def "FindPersistentEntity"() {
        expect:
        GormMetaUtils.findPersistentEntity("Org")
        GormMetaUtils.findPersistentEntity("org")
        GormMetaUtils.findPersistentEntity("gorm.tools.Org")

    }

}

@Entity
class Org {
    String name

    static constraints = {
        name blank: false, inList: ['Joe']
    }
}


