package yakworks.rally.org

import gorm.tools.testing.TestDataJson
import org.springframework.dao.DataRetrievalFailureException

import spock.lang.Ignore
import yakworks.rally.orgs.model.Contact

import java.time.LocalDate

import org.springframework.core.NestedExceptionUtils

import gorm.tools.repository.errors.EntityValidationException
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.model.OrgMember
import spock.lang.Specification
import gorm.tools.source.SourceType
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.OrgRepo
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.testing.MockData

@Integration
@Rollback
class OrgTests extends Specification implements DomainIntTest {

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
        def org = orgRepo.create(params)
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

    @Ignore //XXX fix so it works here
    def "test create duplicate fail"() {
        when:
        def params = MockData.createOrg
        params.num = 'TTI'
        def org = orgRepo.create(params)
        orgRepo.flush()
        //do it again
        org = orgRepo.create(params)
        orgRepo.flush()

        then:
        RuntimeException ge = thrown()
        def rootCause = NestedExceptionUtils.getRootCause(ge)
        rootCause.message.contains("Unique index or primary key violation") || //mysql and H2
            rootCause.message.contains("Duplicate entry") || //mysql
            rootCause.message.contains("Violation of UNIQUE KEY constraint") //sql server

    }

    def "test null num fails"() {
        when:
        def params = [
            name: 'testComp',
            type: 'Customer'
        ]
        orgRepo.create(params)
        orgRepo.flush()

        then:
        EntityValidationException exception = thrown()
        exception.errors.objectName == 'yakworks.rally.orgs.model.Org'
        exception.errors['num'].code == "nullable"
    }

    @Ignore //XXX fix so it works here
    def "change key contact"() {
        when:
        //cust 205 has 2 contacts, 51 and 52, test db has 51 setup and we change it to 52
        assert Org.get(205).contact.id == 51

        def params = [
            id        : 205,
            contact: [
                id: 52
            ]
        ]
        orgRepo.update(params)
        flushAndClear()

        then:
        Org.get(205).contact.id == 52
    }

    @Ignore //XXX fix so it works here
    def "update org"() {
        when:
        def params = MockData.updateOrg
        params.id = 205 //use existing 205 customer that has
        def org = orgRepo.update(params)
        orgRepo.flushAndClear()
        org = Org.get(205)

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

    def testOrgSourceChange() {
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

    @Ignore //XXX https://github.com/9ci/domain9/issues/493 fix so it works here
    void "test insert with orgmembers"() {
        given:
        orgDimensionService.testInit('Branch.Division.Business')
        Org division = Org.create("Division", "Division", OrgType.Division).persist()
        division.member = OrgMember.make(division)
        division.member.business = Org.create("Business", "Business", OrgType.Business).persist()
        division.persist()
        division.member.persist()

        Map params = [name: "test", num: "test", orgTypeId: 3, member: [division: [id: division.id]]]

        when:
        Org result = Org.create(params)

        then:
        result != null
        result.name == "test"
        result.num == "test"
        result.member != null
        result.member.division.id == division.id
        result.member.business.id == division.member.business.id

        when:
        Org otherBusiness = Org.create("b2", "b2", OrgType.Business).persist([flush: true])
        params = [
            name: "test", num: "test", orgTypeId: "3",
            member: [
                division: [id: division.id],
                business: [id: otherBusiness.id]
            ]
        ]
        result = orgRepo.create(params)

        then: "Specified business should NOT take precedence parents set from division"
        result.member.business == division.member.business

        cleanup:
        orgDimensionService.testInit(null)
    }

    @Ignore //XXX fix so it works here
    def "delete should fail when source is ERP"() {
        when:
        def org = Org.get(205)
        assert org.source.sourceType == SourceType.ERP
        orgRepo.removeById(org.id)

        then:
        EntityValidationException e = thrown()
        e.code == 'delete.error.source.external'
    }



    //https://github.com/9ci/domain9/issues/268 need better tests for success delete, make sure contacts are deleted, etc..
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

    void "test find org by sourceid"() {
        when:
        Org org = Org.create(num: "foo", name: "bar", type: OrgType.Customer)
        // org.persist()
        // org.createSource()
        orgRepo.flush()

        then: "source id is the default"
        assert org.source.sourceId == "foo"

        Org o = Org.repo.findWithData([source: [sourceId: 'foo', orgType: 'Customer']])
        o.name == "bar"

    }

    void "test find org by num"() {
        when:
        Org org3 = Org.create(num: "foo3", name: "bar3", type: OrgType.Customer)
        orgRepo.flush()

        Org o3 = Org.repo.findWithData(num: "foo3")

        then: "found because unique"
        assert o3
    }

    void "test find org by num not unique"() {
        when:
        Org org = Org.create(num: "foo", name: "bar", type: OrgType.Customer)
        Org org2 = Org.create(num: "foo", name: "bar2", type: OrgType.CustAccount)
        orgRepo.flush()
        Org o3 = Org.repo.findWithData(num: "foo")

        then: "not found because not unique"
        thrown DataRetrievalFailureException

        when: "num would get set to sourceId so it will fail too"
        o3 = Org.repo.findWithData(source:[ sourceId: "foo"])

        then: "not found because not unique"
        thrown DataRetrievalFailureException
    }

    def "update org lookup by sourceid"() {
        setup:
        Long orgId = 1111

        Map params = TestDataJson.buildMap(Org) << [id: orgId, name: 'name', num: 'foo', type: 'Customer']

        when: "create"
        def org = Org.create(params, bindId: true)
        orgRepo.flush()

        then: "make sure source is assigned properly"
        org.id == orgId
        org.source.sourceId == 'foo'
        org.source.orgType == OrgType.Customer
        // use the same query orgSource.repo is using
        List res = OrgSource.executeQuery('select orgId from OrgSource where sourceId = :sourceId and orgType = :orgType',
            [sourceId: 'foo', orgType: OrgType.Customer] )
        res.size() == 1

        when: "update"
        org = Org.update([source: [sourceId: 'foo', orgType: 'Customer'], name: 'new name'])

        then:
        org.name == 'new name'
        org.source.sourceId == 'foo'
    }

    def "create org with member branch lookup by num"() {
        when:
        Long orgId = 1111

        Map params = TestDataJson.buildMap(Org) << [id: orgId, name: 'name', num: 'foo', type: 'Customer', member: [branch: [id: 30 ]]]
        def org = Org.create(params, bindId: true)
        orgRepo.flush()

        then: "make sure member is created with branch"
        org.id == orgId
        org.member
        30 == org.member.branch.num
    }

}
