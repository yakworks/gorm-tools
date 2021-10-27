package yakworks.rally.org


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.gorm.testing.DomainIntTest

@Integration
@Rollback
class ContactTests extends Specification implements DomainIntTest {

    void "test create Contact with org lookup by orgSource"() {
        setup:
        Org org = Org.create("foo", "bar", OrgType.Customer)
        org.validate()
        org.createSource()
        org.persist(flush: true)

        Map params = [firstName:'Peter', email:'abc@walmart.com']
        params.org = [source: [sourceId: 'foo', orgType: OrgType.Customer.name()]]


        when:
        def entity = Contact.create(params)
        flushAndClear()
        def contact = Contact.get(entity.id)

        then:
        contact.org.num == "foo"

    }


}
