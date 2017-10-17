package gorm.tools

import grails.test.hibernate.HibernateSpec
import grails.test.mixin.TestMixin
import grails.test.mixin.gorm.Domain
import grails.test.mixin.hibernate.HibernateTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.gorm.annotation.Entity

//@TestMixin(GrailsUnitTestMixin)
@Domain([Org])
@TestMixin(HibernateTestMixin)
class GormMetaUtilsSpec extends Specification {

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

    List<Class> getDomainClasses() {
        return [Org]
    }
}

@Entity
class Org {
    String name

    static constraints = {
        name blank: false, inList: ['Joe']
    }
}


