package gorm.tools

import yakworks.taskify.domain.Org
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
        GormMetaUtils.getPersistentEntity("yakworks.taskify.domain.Org")
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
        GormMetaUtils.findPersistentEntity("yakworks.taskify.domain.Org")
    }

    def "GetPersistentEntity name string"() {
        expect:
        GormMetaUtils.getPersistentEntity("yakworks.taskify.domain.Org")
    }

    def "GetPersistentEntity instance"() {
        expect:
        GormMetaUtils.getPersistentEntity(Org.get(100))
    }

    def "GetPersistentEntity"() {
        expect:
        GormMetaUtils.getPersistentEntity(Org)
    }

}
