package gorm.tools

import grails.gorm.annotation.Entity
import grails.test.hibernate.HibernateSpec
import grails.testing.gorm.DomainUnitTest

class GormMetaUtilsSpec extends HibernateSpec implements DomainUnitTest<Org> {

    List<Class> getDomainClasses() { [Org] }

    static doWithSpring = {

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


