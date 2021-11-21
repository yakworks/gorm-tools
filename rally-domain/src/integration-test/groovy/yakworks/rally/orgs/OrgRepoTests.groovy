package yakworks.rally.orgs

import java.time.LocalDate

import org.springframework.core.NestedExceptionUtils
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DataRetrievalFailureException

import gorm.tools.problem.ValidationProblem
import gorm.tools.model.SourceType
import gorm.tools.testing.TestDataJson
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.problem.data.OptimisticLockingProblem
import yakworks.problem.data.UniqueConstraintProblem
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.OrgRepo
import yakworks.rally.tag.model.Tag
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

    void "create with tags"() {
        when: "Create a test tag"
        Tag tag1 = Tag.create(code: 'foo')
        def cust = orgRepo.create([num:"C1", name:"C1", type: 'Customer', tags:[[id:tag1.id]]])
        flush() //flush to db because the query that gets tags will show 0 if not

        then:
        tag1
        cust.tags.size() == 1
        cust.tags[0].code == 'foo'
    }

    @IgnoreRest
    void "test create duplicate fail"() {
        when:
        def params = MockData.createOrg
        params.num = '99' //should already exist in test db
        //flush during create so it forces the error catching
        // def org = orgRepo.create(params.asUnmodifiable(), [flush: true])
        def org = orgRepo.create(params.asUnmodifiable())
        // def org = orgRepo.create(params.asUnmodifiable())
        orgRepo.flush()

        then:
        thrown DataIntegrityViolationException
        // UniqueConstraintProblem ge = thrown()
        // def rootCause = NestedExceptionUtils.getRootCause(ge)
        // ge.detail.contains("Unique index or primary key violation") || //mysql and H2
        //     ge.detail.contains("Duplicate entry") || //mysql
        //     ge.detail.contains("Violation of UNIQUE KEY constraint") || //sql server
        //     ge.detail.contains("duplicate key value violates unique constraint") //postgres

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
        ValidationProblem exception = thrown()
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

    def "delete should fail when source is ERP"() {
        when:
        def org = Org.get(99)
        org.source.sourceType = SourceType.ERP
        org.source.persist(flush:true)
        orgRepo.removeById(org.id)

        then:
        ValidationProblem e = thrown()
        e.code == 'error.delete.externalSource'
    }

    //XXX https://github.com/9ci/domain9/issues/268 need better tests for success delete, make sure contacts are deleted, etc..
    @Ignore //FIXME whats the scoop with this one?
    def "delete contact with org"() {
        when:
        def org = Org.get(9)
        def contact = Contact.get(9)
        def contact2 = Contact.findWhere(num: 'secondary9')

        then:
        contact
        contact2
        org.contact == contact

        then:
        org.remove()  // orgRepo.remove or orgRepo.removeById is not removing either
        flush()

        then:
        !Org.findById(9)
        !Contact.get(9)
        !Contact.findWhere(num: 'secondary9')
        !Contact.findAllByOrg(org)
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
        when: "findWithData has orgType on source"
        Org org = Org.create(num: "foo", name: "bar", type: OrgType.Customer)
        orgRepo.flush()
        assert org.source.sourceId == "foo"

        Org o = Org.repo.findWithData([source: [sourceId: 'foo', orgType: 'Customer']])

        then: "source id is the default"
        o

        when: "findWithData has type in daya"
        flushAndClear()
        o = Org.repo.findWithData([type: 'Customer', source: [sourceId: 'foo']])

        then: "source id is the default"
        o

    }

    void "test find org by num"() {
        when:
        Org org3 = Org.create(num: "foo3", name: "bar3", type: OrgType.Customer)
        orgRepo.flush()

        Org o3 = Org.repo.findWithData(num: "foo3")

        then: "found because unique"
        assert o3

        when:
        o3 = Org.repo.findWithData(num: "foo3", type: OrgType.Customer)

        then: "also found"
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

    void "test find org by num not unique with type"() {
        when:
        Org org = Org.create(num: "foo", name: "bar", type: OrgType.Customer)
        Org org2 = Org.create(num: "foo", name: "bar2", type: OrgType.CustAccount)
        orgRepo.flush()
        Org o3 = Org.repo.findWithData(num: "foo", type: OrgType.Customer)

        then: "found because data has type"
        o3

        when: "will uses type"
        o3 = Org.repo.findWithData(source:[ sourceId: "foo"], type: OrgType.Customer)

        then: "not found because not unique"
        o3
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

    @Ignore // xxx https://github.com/9ci/domain9/issues/493
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
