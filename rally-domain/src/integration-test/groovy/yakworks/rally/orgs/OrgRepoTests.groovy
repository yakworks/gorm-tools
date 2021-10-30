package yakworks.rally.orgs

import java.time.LocalDate

import org.springframework.core.NestedExceptionUtils

import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.model.SourceType
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.OrgRepo
import yakworks.rally.testing.MockData

@Integration
@Rollback
class OrgRepoTests extends Specification implements DomainIntTest {

    OrgRepo orgRepo
    OrgDimensionService orgDimensionService

    def "test new org"() {
        expect:
        def o = new Org(num: '123', name: 'foo', type: OrgType.Customer)
        o.persist(flush: true)
    }

    def "test simple create sanity check"() {
        expect:
        def params = [
            num: '0011',
            name: 'testComp',
            type: 'Customer'
        ]
        orgRepo.create(params)
        orgRepo.flushAndClear()
    }

    def "test create"() {
        when:
        def params = MockData.createOrg
        def org = orgRepo.create(params.asUnmodifiable())
        orgRepo.flushAndClear()
        org = Org.get(org.id)

        then:
        org
        org.num == params.num
        org.name == params.name
        org.companyId == params.companyId
        org.type == OrgType.Customer
        org.location
        org.location.org == org
        ['zipCode', 'street1', 'street2', 'city', 'state', 'country'].each{
            org.location[it] == params.location[it]
        }

        org.contact
        org.contact.org == org
        org.contact.firstName == params.contact.firstName

        org.flex
        org.flex.id == org.id
        org.flex.text1 == params.flex.text1
        org.flex.num1 == params.flex.num1
        org.flex.date1.toLocalDate() == LocalDate.parse(params.flex.date1)

        org.info
        org.info.phone == params.info.phone

        org.locations.size() == 3 //the location + what was in the locations

        //source should be created
        org.source
        org.source.sourceId == params.num
        org.source.orgId == org.id
        org.source.orgType == OrgType.Customer
        org.source.sourceType == SourceType.App

    }

    def "test create duplicate fail"() {
        when:
        def params = MockData.createOrg
        params.num = '99' //should already exist in test db
        def org = orgRepo.create(params.asUnmodifiable())
        orgRepo.flush()

        then:
        RuntimeException ge = thrown()
        def rootCause = NestedExceptionUtils.getRootCause(ge)
        rootCause.message.contains("Unique index or primary key violation") || //mysql and H2
            rootCause.message.contains("Duplicate entry") || //mysql
            rootCause.message.contains("Violation of UNIQUE KEY constraint") || //sql server
            rootCause.message.contains("duplicate key value violates unique constraint") //postgres

    }

    def "test null num fails"() {
        when:
        def params = [
            name: 'testComp',
            type: 'Customer'
        ]
        orgRepo.create(params.asUnmodifiable())
        orgRepo.flush()

        then:
        EntityValidationException exception = thrown()
        exception.errors.objectName == 'yakworks.rally.orgs.model.Org'
        exception.errors['num'].code == "nullable"
    }

    def "change key contact"() {
        when:
        assert Org.get(99).contact.num == 'primary99'
        def c2 = Contact.findByNum('secondary99')

        def params = [
            id        : 99,
            contact: [
                id: c2.id
            ]
        ]
        orgRepo.update(params.asUnmodifiable())
        flushAndClear()

        then:
        Org.get(99).contact.id == c2.id
    }

    def "update org"() {
        when:
        def params = MockData.updateOrg
        params.id = 99 //use existing 205 customer that has
        def org = orgRepo.update(params.asUnmodifiable())
        orgRepo.flushAndClear()
        org = Org.get(99)

        then:
        org
        org.num == params.num
        org.name == params.name

        org.location.zipCode == params.location.zipCode

        org.contact.firstName == params.contact.firstName

        org.flex
        org.flex.id == org.id
        org.flex.text1 == params.flex.text1

        org.info
        org.info.phone == params.info.phone

        //org.locations.size() == 3 //the location + what was in the locations
    }

    def "change sourceId for Org"() {
        //simulates the customer and cust account setup as well.
        when:
        Org org = Org.create("foo", "bar", OrgType.CustAccount, 2)
        org.validate()
        org.createSource()
        org.persist()

        then: "source id is the default from num"
        assert org.source.sourceId == "foo"

        when: "sourceId is changed"
        org.source.sourceId = "test"
        org.source.persist()
        Long osi = org.source.id

        then: "it should be good"
        assert org.source.sourceId == "test"
        //get from cache
        assert OrgSource.get(osi).sourceId == "test"

        when: "flush and clear is called and OrgSource is retreived again"
        flushAndClear()

        then : "should stil; be the sourceId that was set"
        assert OrgSource.get(osi).sourceId == "test"
    }

    void "add location to existing Org"() {
        when:
        def o = new Org(num: '123', name: 'foo', type: OrgType.Customer)
        o.persist()
        flushAndClear()
        Map data = [
            id: o.id,
            location: [
                city: 'Denver',
                street1: '1st'
            ]
        ]
        def org = orgRepo.update(data)

        then:
        org.location
        org.location.city == 'Denver'
    }

    def "delete should fail when source is ERP"() {
        when:
        def org = Org.get(99)
        org.source.sourceType = SourceType.ERP
        org.source.persist(flush:true)
        orgRepo.removeById(org.id)

        then:
        EntityValidationException e = thrown()
        e.code == 'delete.error.source.external'
    }

    //XXX https://github.com/9ci/domain9/issues/268 need better tests for success delete, make sure contacts are deleted, etc..
    @Ignore //FIXME whats the scoop with this one?
    def "delete contact with org"() {
        when:
        def org = Org.get(101)
        def contact = Contact.get(50)
        assert org.contact == contact
        org.remove()  // orgRepo.remove or orgRepo.removeById is not removing either

        then:
        null == Org.findById(101)
        null == Contact.get(50)
        null == Contact.findAllByOrg(org)
    }

    def "test create Org different orgType same sourceId"() {
        when:
        def params = MockData.createOrg
        def orgCustomer = orgRepo.create(params)
        orgRepo.flushAndClear()
        orgCustomer = Org.get(orgCustomer.id)

        params.type = "CustAccount"
        def orgCustAccount = orgRepo.create(params)
        orgRepo.flushAndClear()
        orgCustAccount = Org.get(orgCustAccount.id)

        then:
        orgCustomer
        orgCustomer.type == OrgType.Customer
        OrgSource source = OrgSource.findByOrgId(orgCustomer.id)
        source.sourceId == params.num
        source.orgType == OrgType.Customer
        source.sourceType == SourceType.App

        orgCustAccount
        orgCustAccount.type == OrgType.CustAccount
        OrgSource source2 = OrgSource.findByOrgId(orgCustAccount.id)
        source2.sourceId == params.num
        source2.orgType == OrgType.CustAccount
        source2.sourceType == SourceType.App

    }


}
