package gorm.tools

import spock.lang.Specification
import testing.Jumper
import daoapp.Org

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class GormMetaUtilsSpec extends Specification {

    def "GetDomainClass"() {
        expect:
        GormMetaUtils.getDomainClass(Org)
    }

    def "GetDomainClass string"() {
        expect:
        GormMetaUtils.getDomainClass("daoapp.Org")
    }

    def "GetDomainClass instance"() {
        expect:
        def o = Org.get(100)
        assert o.dao
        GormMetaUtils.getDomainClass(o)
    }

    def "findDomainClass"() {
        expect:
        GormMetaUtils.findDomainClass("Org")
        GormMetaUtils.findDomainClass("org")
        GormMetaUtils.findDomainClass("daoapp.Org")
        GormMetaUtils.findDomainClass("dropZone")
        GormMetaUtils.findDomainClass("DropZone")
        //GormMetaUtils.findDomainClass("dropzone")
    }

    def "GetPersistentEntity name string"() {
        expect:
        GormMetaUtils.getPersistentEntity("daoapp.Org")
    }

    def "GetPersistentEntity instance"() {
        expect:
        GormMetaUtils.getPersistentEntity(Org.get(100))
    }

    def "GetPersistentEntity"() {
        expect:
        GormMetaUtils.getPersistentEntity(Org)
    }

    def "FindPersistentEntity"() {
        expect:
        GormMetaUtils.findPersistentEntity("Org")
        GormMetaUtils.findPersistentEntity("org")
        GormMetaUtils.findPersistentEntity("daoapp.Org")

    }
}
