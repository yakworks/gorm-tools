package gorm.tools.security.stamp

import gorm.tools.security.audit.AuditStamp
import gorm.tools.testing.unit.DomainRepoTest
import grails.persistence.Entity
import spock.lang.Specification

class TestDomainTests extends Specification implements DomainRepoTest<TestDomain> {

    void testBasics() {
        when:
        TestDomain data = new TestDomain()

        then:
        ['createdDate','editedDate','createdBy','editedBy'].each{key->
            assert data.hasProperty(key)
        }
    }


    void test_new_bindable_SanityCheck() {
        when: "binding occurs"
        TestDomain d = new TestDomain()
        d.properties = [name: 'test', createdBy:99, editedBy:99]

        then: "should not have been bound"
        //assert config.grails.plugin.audittrail
        d.createdBy == null
        d.editedBy == null

        when: "new binding occurs"
        TestDomain d2 = new TestDomain()
        d2.bind([name:'test', createdBy:99, createdDate: new Date(), editedBy:99, editedDate: new Date()])

        then: "should not have been bound"
        //assert config.grails.plugin.audittrail
        d2.createdBy == null
        d2.createdDate == null
        d2.editedBy == null
        d2.editedDate == null
    }
}

@AuditStamp
@Entity
class TestDomain implements Serializable {

    String name

    static mapping = {
        table 'TestDomains'
    }

}
