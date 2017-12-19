package gorm.tools

import repoapp.Org
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
        GormMetaUtils.getPersistentEntity("repoapp.Org")
    }

    def "GetDomainClass instance"() {
        expect:
        def o = Org.get(100)
        assert o.repo
        GormMetaUtils.getPersistentEntity(o)
    }

    def "findDomainClass"() {
        expect:
        GormMetaUtils.findPersistentEntity("Org")
        GormMetaUtils.findPersistentEntity("org")
        GormMetaUtils.findPersistentEntity("repoapp.Org")
        GormMetaUtils.findPersistentEntity("dropZone")
        GormMetaUtils.findPersistentEntity("DropZone")
        //GormMetaUtils.findDomainClass("dropzone")
    }

    def "GetPersistentEntity name string"() {
        expect:
        GormMetaUtils.getPersistentEntity("repoapp.Org")
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
        GormMetaUtils.findPersistentEntity("repoapp.Org")

    }
}
