package gorm.tools

import daoapp.Org
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class GormMetaUtils2Spec extends Specification {

    def "GetDomainClass"() {
        expect:
        GormMetaUtils.getPersistentEntity(Org)
    }

    def "GetDomainClass string"() {
        expect:
        GormMetaUtils.getPersistentEntity("daoapp.Org")
    }

    def "GetDomainClass instance"() {
        expect:
        def o = Org.get(100)
        assert o.dao
        GormMetaUtils.getPersistentEntity(o)
    }

    def "findDomainClass"() {
        expect:
        GormMetaUtils.findPersistentEntity("Org")
        GormMetaUtils.findPersistentEntity("org")
        GormMetaUtils.findPersistentEntity("daoapp.Org")
        GormMetaUtils.findPersistentEntity("dropZone")
        GormMetaUtils.findPersistentEntity("DropZone")
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
