package yakworks.rally.orgs

import yakworks.commons.map.Maps
import yakworks.rally.orgs.model.Company
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.OrgCalc
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgInfo
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.orgs.model.PartitionOrg

import java.time.LocalDate

import org.springframework.dao.DataRetrievalFailureException

import gorm.tools.model.SourceType
import gorm.tools.problem.ValidationProblem
import yakworks.rally.testing.OrgDimensionTesting
import yakworks.testing.gorm.TestDataJson
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.api.problem.data.DataProblemException
import yakworks.api.problem.data.DataProblemCodes
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
            assert org.location[it] == params.location[it].trim()
        }

        jdbcTemplate.queryForObject("select orgId from Contact where id = $org.contact.id", Long) == org.id

        org.contact.firstName == params.contact.firstName
        org.contact
        org.contact.org
        org.contact.org == org
        org.contact.firstName == params.contact.firstName
        org.contact.location.city == 'Gulch'
        org.contact.locations.size() == 2
        org.contact.locations[1].zipCode == "12345"

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

    void "test create duplicate fail"() {
        when:
        def params = MockData.createOrg
        params.num = '10' //should already exist in test db
        //flush during create so it forces the error catching
        def org = orgRepo.create(Maps.clone(params), [flush: true])
        // orgRepo.flush()

        then:
        // thrown DataIntegrityViolationException
        DataProblemException ge = thrown()
        def problem = ge.problem
        ge.code == DataProblemCodes.UniqueConstraint.code

        // def rootCause = NestedExceptionUtils.getRootCause(ge)
        problem.code == DataProblemCodes.UniqueConstraint.code
        problem.detail.contains("Violates unique constraint")
    }

    def "test null num fails"() {
        when:
        def params = [
            name: 'testComp',
            type: 'Customer'
        ]
        orgRepo.create(Maps.clone(params))
        orgRepo.flush()

        then:
        ValidationProblem.Exception exception = thrown()
        exception.errors.objectName == 'yakworks.rally.orgs.model.Org'
        exception.errors['num'].code == "NotNull"
    }

    def "change key contact"() {
        when:
        assert Org.get(10).contact.id == 10
        def c2 = Contact.findByNum('secondary10')
        assert c2.id != 10

        def params = [
            id        : 10,
            contact: [
                id: c2.id
            ]
        ]
        orgRepo.update(params.asUnmodifiable())
        flushAndClear()

        then:
        Org.get(10).contact.id == c2.id
    }

    def "update org"() {
        when:
        def params = MockData.updateOrg
        params.id = 9 //use existing 205 customer that has
        def org = orgRepo.update(params.asUnmodifiable())
        orgRepo.flushAndClear()
        org = Org.get(9)

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
        Org org = Org.of("foo", "bar", OrgType.CustAccount, 2)
        Org.repo.createSource(org)
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

    void "update existing primary location"() {
        setup:
        Org org = Org.get(11)

        expect:
        org
        org.location

        int countBefore = Location.count()

        when:
        Location existingLocation = org.location
        Map data = [
            id: org.id,
            location: [
                city: 'Denver',
                street1: '1st'
            ],
        ]
        org = orgRepo.update(data)
        flush()
        int countAfter = Location.count()

        then: "existing primary location should have been updated"
        noExceptionThrown()
        org.location
        org.location.id == existingLocation.id
        org.location.city == "Denver"
        org.location.street1 == "1st"

        and: "no new locations added"
        countBefore == countAfter
    }

    void "should not create empty locations"() {
        setup:
        Org org = Org.get(11)
        int countBefore = Location.count()

        when:
        Map data = [
            id: org.id,
            locations:[
                [:]
            ]
        ]
        orgRepo.update(data)
        flushAndClear()
        int countAfter = Location.count()

        then: "no new location should be added"
        noExceptionThrown()
        countBefore == countAfter
    }

    void "test insert with orgmembers"() {
        given:
        OrgDimensionTesting.setDimensions(['Branch', 'Division', 'Business'])
        Org division = Org.of("Division", "Division", OrgType.Division).persist()
        division.member = OrgMember.make(division)
        division.member.business = Org.of("Business", "Business", OrgType.Business).persist()
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
        result.member.company
        result.member.companyId == Company.DEFAULT_COMPANY_ID

        when:
        Org otherBusiness = Org.of("b2", "b2", OrgType.Business).persist([flush: true])
        params = [
            name: "test", num: "test2", orgTypeId: "3",
            member: [
                division: [id: division.id],
                business: [id: otherBusiness.id]
            ]
        ]
        result = orgRepo.create(params)

        then: "Specified business should NOT take precedence parents set from division"
        result.member.business == division.member.business

        cleanup:
        OrgDimensionTesting.resetDimensions()
    }

    void "delete should fail when source is ERP"() {
        when:
        def org = Org.get(9)
        org.source.sourceType = SourceType.ERP
        org.source.persist(flush:true)
        orgRepo.removeById(org.id)

        then:
        ValidationProblem.Exception e = thrown()
        e.code == 'error.delete.externalSource'
    }

    void "delete associated domains with org"() {
        when:
        Org org = Org.get(10)
        Contact contact = Contact.get(10)
        Contact contact2 = Contact.findWhere(num: 'secondary10')
        OrgCalc calc = new OrgCalc(id:org.id).persist()
        org.calc = calc
        assert org.member
        // org.member = OrgMember.make(org).persist()
        // org.persist(flush:true)

        then:
        contact
        contact2
        org.contact == contact
        contact2.org == org
        org.flex != null
        org.info != null
        OrgInfo.get(org.id) != null
        OrgCalc.get(org.id) != null
        OrgSource.findByOrgId(org.id) != null
        org.source != null
        OrgMember.get(org.id) != null
        org.location != null

        when:
        org.remove()  // orgRepo.remove or orgRepo.removeById is not removing either
       // flush()

        then:
        !Org.get(10)
        !Contact.exists(10L)
        !Contact.findWhere(num: 'secondary10')
        !Contact.findAllByOrg(org)
        !OrgFlex.get(org.id)
        !OrgCalc.get(10)
        !OrgSource.findByOrgId(org.id)
        !OrgMember.get(org.id)
        !OrgInfo.get(org.id)
        !Location.findAllByOrg(org)
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

    void "test lookup org by sourceid"() {
        setup: "findWithData has orgType on source"
        Org org = Org.create(num: "foo", name: "bar", type: OrgType.Customer)
        orgRepo.flush()

        expect:
        org.source.sourceId == "foo"
        orgRepo.lookup([source: [sourceId: 'foo', orgType: 'Customer']])

        when:
        Org o = orgRepo.findWithData([source: [sourceId: 'foo', orgType: 'Customer']])

        then: "source id is the default"
        o != null

        when: "findWithData has type in daya"
        flushAndClear()
        o = orgRepo.findWithData([type: 'Customer', source: [sourceId: 'foo']])

        then: "source id is the default"
        o

        expect: "lookup by sourceid contained in org map"
        orgRepo.lookup([type: 'Customer', org:[source: [sourceId: 'foo']]]) != null
    }


    void "test lookup org by num"() {
        setup:
        Org.create(num: "foo3", name: "bar3", type: OrgType.Customer)
        orgRepo.flush()

        expect:
        orgRepo.lookup(num: "foo3")

        when:
        Org o3 = orgRepo.findWithData(num: "foo3")

        then: "found because unique"
        o3 != null

        when:
        o3 = orgRepo.findWithData(num: "foo3", type: OrgType.Customer)

        then: "also found"
        o3 != null
    }

    void "test find org by num not unique"() {
        setup:
        Org.create(num: "foo", name: "bar", type: OrgType.Customer)
        Org.create(num: "foo", name: "bar2", type: OrgType.CustAccount)
        orgRepo.flush()

        when:
        Org o3 = Org.repo.findWithData(num: "foo")

        then: "not found because not unique"
        thrown DataRetrievalFailureException

        when: "num would get set to sourceId so it will fail too"
        o3 = Org.repo.findWithData(source:[ sourceId: "foo"])

        then: "not found because not unique"
        thrown DataRetrievalFailureException
    }

    void "test lookup org by num not unique with type"() {
        setup:
        Org.create(num: "foo", name: "bar", type: OrgType.Customer)
        Org.create(num: "foo", name: "bar2", type: OrgType.CustAccount)
        orgRepo.flush()

        expect:
        orgRepo.lookup(num: "foo", type: OrgType.Customer)

        when:
        Org o3 = orgRepo.findWithData(num: "foo", type: OrgType.Customer)

        then: "found because data has type"
        o3

        when: "will uses type"
        o3 = orgRepo.findWithData(source:[ sourceId: "foo"], type: OrgType.Customer)

        then: "not found because not unique"
        o3
    }

    void "update org lookup by sourceid"() {
        setup:
        Long orgId = 1111

        Map params = TestDataJson.buildMap(Org) << [id: orgId, name: 'name', num: 'foo', type: 'Customer']

        when: "create"
        def org = Org.repo.create(params, [bindId: true])
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
        org = Org.repo.update([source: [sourceId: 'foo', orgType: 'Customer'], name: 'new name'])

        then:
        org.name == 'new name'
        org.source.sourceId == 'foo'

        when: "just sourceid is passed"
        def result = orgRepo.lookup(sourceId:"foo", type:org.type)

        then:
        result == org
    }

    void "create org with member branch lookup by num"() {
        setup:
        OrgDimensionTesting.setDimensions([OrgType.Customer, OrgType.Branch])
        Org branch = Org.findByOrgTypeId(OrgType.Branch.id)

        expect:
        branch != null

        when:
        Long orgId = 1111

        Map params = TestDataJson.buildMap(Org) << [id: orgId, name: 'name', num: 'foo', type: 'Customer', member: [branch: [id: branch.id ]]]
        def org = Org.repo.create(params, [bindId: true])
        orgRepo.flush()

        then: "make sure member is created with branch"
        org.id == orgId
        org.member
        branch.id == org.member.branch.id

        cleanup:
        OrgDimensionTesting.resetDimensions()
    }

    void "create and update partition org"() {
        when: "orgtype is not partition type"
        Org org = Org.create(num: '0011', name: 'testComp', type: 'Customer')
        flush()

        then:
        org
        !PartitionOrg.findWhere(num:"0011")

        when: "type=partition type"
        org = Org.create(num: '0012', name: 'testComp', type: 'Company')
        flush()

        PartitionOrg porg = PartitionOrg.findWhere(num:"0012")

        then:
        org
        porg
        org.id == porg.id
        org.name == porg.name
        org.num == porg.num

        when:
        Org.update(id:org.id, name:"updated", num:"updated")
        flushAndClear()
        porg = PartitionOrg.findWhere(num:"updated")

        then:
        porg.name == "updated"
        porg.name == "updated"

        when: "delete"
        orgRepo.removeById(org.id)
        flush()

        then:
        !PartitionOrg.get(org.id)
    }

}
