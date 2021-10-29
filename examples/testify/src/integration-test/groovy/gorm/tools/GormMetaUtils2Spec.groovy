package gorm.tools

import gorm.tools.utils.GormMetaUtils
import yakworks.rally.orgs.model.Org
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.OrgTag

@Integration
@Rollback
class GormMetaUtils2Spec extends Specification {

    def "GetDomainClass"() {
        expect:
        GormMetaUtils.getPersistentEntity(Org)
    }

    def "GetDomainClass string"() {
        expect:
        GormMetaUtils.getPersistentEntity("yakworks.rally.orgs.model.Org")
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
        GormMetaUtils.findPersistentEntity("yakworks.rally.orgs.model.Org")
    }

    def "GetPersistentEntity name string"() {
        expect:
        GormMetaUtils.getPersistentEntity("yakworks.rally.orgs.model.Org")
    }

    def "GetPersistentEntity instance"() {
        expect:
        GormMetaUtils.getPersistentEntity(Org.get(1))
    }

    def "GetPersistentEntity"() {
        expect:
        GormMetaUtils.getPersistentEntity(Org)
    }

    void "check domain with composite id"() {
        expect:
        GormMetaUtils.findPersistentEntity(OrgTag.simpleName).persistentProperties.size() == 3
    }

}
