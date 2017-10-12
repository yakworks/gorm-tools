package gorm.tools

import grails.persistence.Entity
import grails.test.hibernate.HibernateSpec
import grails.test.mixin.TestMixin
import grails.test.mixin.gorm.Domain
import grails.test.mixin.hibernate.HibernateTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
//import grails.gorm.annotation.Entity

@Integration
class GormMetaUtilsIntSpec extends Specification {

    static doWithSpring = {

    }

    def "GetDomainClass"() {
        expect:
        GormMetaUtils.getDomainClass(Orgify)
    }


    def "GetDomainClass string"() {
        expect:
        GormMetaUtils.getDomainClass("gorm.tools.Orgify")
    }

    def "GetDomainClass instance"() {
        expect:
        def o = new Orgify()
        assert o.dao
        GormMetaUtils.getDomainClass(o)
    }

    def "findDomainClass"() {
        expect:
        GormMetaUtils.findDomainClass("Orgify")
        GormMetaUtils.findDomainClass("Orgify")
        GormMetaUtils.findDomainClass("gorm.tools.Orgify")
    }

    def "GetPersistentEntity name string"() {
        expect:
        GormMetaUtils.getPersistentEntity("gorm.tools.Orgify")
    }

    def "GetPersistentEntity instance"() {
        expect:
        GormMetaUtils.getPersistentEntity(new Orgify())
    }

    def "GetPersistentEntity"() {
        expect:
        GormMetaUtils.getPersistentEntity(Orgify)
    }

    def "FindPersistentEntity"() {
        expect:
        GormMetaUtils.findPersistentEntity("Orgify")
        GormMetaUtils.findPersistentEntity("Orgify")
        GormMetaUtils.findPersistentEntity("gorm.tools.Orgify")

    }

}

@Entity
class Orgify {
    String name

    static constraints = {
        name blank: false, inList: ['Joe']
    }
}


