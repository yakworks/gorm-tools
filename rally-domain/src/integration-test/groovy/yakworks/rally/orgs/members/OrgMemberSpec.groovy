package yakworks.rally.orgs.members

import org.springframework.validation.Errors

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import yakworks.rally.orgs.model.Company
import yakworks.rally.testing.OrgDimensionTesting
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.api.problem.data.DataProblemException
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.orgs.model.OrgType
import spock.lang.Specification
import spock.lang.Unroll
import yakworks.rally.orgs.repo.OrgRepo

@Integration
@Rollback
class OrgMemberSpec extends Specification implements DomainIntTest {

    OrgRepo orgRepo

    void emptyDimensions(){
        OrgDimensionTesting.emptyDimensions()
    }

    void cleanupSpec() {
        OrgDimensionTesting.resetDimensions()
    }

    def "test sanity check on orgMember"() {
        setup:
        emptyDimensions()
        Org org = Org.of("O1", "O1", OrgType.Customer).persist()
        Org branch = Org.of("Branch", "Branch", OrgType.Branch).persist()
        Org division = Org.of("Division", "Division", OrgType.Division).persist()

        expect:
        org.companyId != null

        when:
        OrgMember member = OrgMember.make(org)

        then:
        member

        when:
        org.member = member
        org.persist(flush: true)
        flush()

        then:
        OrgMember.get(org.id) != null
        //OrgMember.findByOrg(org) != null

        when:
        member.branch = branch
        member.division = division
        member.persist(flush: true)
        flush()

        then:
        OrgMember.findByBranch(branch) == member
        OrgMember.findByDivision(division) == member
        //OrgMember.findByOrg(org) == member

        OrgMember.findAll("from OrgMember m where m.branch = :branch", [branch:branch]).contains(member)
        jdbcTemplate.queryForObject("select branchId from OrgMember where id = $org.id", Long) == branch.id
        jdbcTemplate.queryForObject("select divisionId from OrgMember where id = $org.id", Long) == division.id
        //jdbcTemplate.queryForObject("select memberId from Org where id = $org.id", Long) == member.id

        when:
        org.delete()
        flush()

        then:
        OrgMember.get(member.id) == null
    }

    def "test getMember"() {
        when:
        Org org = Org.of("O1", "O1", OrgType.Customer).persist()
        Org branch = Org.of("Branch", "Branch", OrgType.Branch).persist()
        Org division = Org.of("Division", "Division", OrgType.Division).persist()
        OrgMember member = OrgMember.make(org)
        org.member = member
        member.branch = branch
        member.division = division
        member.persist(flush: true)
        org.persist(flush: true)
        member.persist(flush: true)
        flush()

        then:
        branch == member.getMemberOrg(OrgType.Branch)
        branch.id == member.getMemberOrgId(OrgType.Branch)
    }

    void "test insert with orgmembers"() {
        given:
        OrgDimensionTesting.setDimensions([OrgType.Branch, OrgType.Division, OrgType.Business])
        Org division = Org.of("Division", "Division", OrgType.Division).persist()
        division.member = OrgMember.make(division)
        division.member.business = Org.of("Business", "Business", OrgType.Business).persist()
        division.persist()
        division.member.persist()

        when:
        Map params = [name: "test", num: "test", orgTypeId: 3, member: [division: [id: division.id]]]
        Org result = Org.create(params)

        then:
        result != null
        result.name == "test"
        result.num == "test"
        result.member != null
        result.member.division.id == division.id
        result.member.business.id == division.member.business.id
        result.member.companyId
        result.member.companyId ==  Company.DEFAULT_COMPANY_ID

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

    }

    void "test create customer with branch by id"() {
        given:
        OrgDimensionTesting.setDimensions([OrgType.Customer, OrgType.Branch])

        when:
        Org branch = Org.findByOrgTypeId(OrgType.Branch.id)
        assert branch
        Map params = [name: "test", num: "test", orgTypeId: 1, member: [branch: [id: branch.id]]]
        Org result = Org.create(params)

        then:
        result != null
        result.name == "test"
        result.num == "test"
        result.member != null
        result.member.branch.id == branch.id
        result.member.company

    }

    void "test create customer with branch by num"() {
        given:
        OrgDimensionTesting.setDimensions([OrgType.Customer, OrgType.Branch])

        when:
        Org branch = Org.findByOrgTypeId(OrgType.Branch.id)
        assert branch
        Map params = [name: "test", num: "test", orgTypeId: 1, member: [branch: [num: branch.num]]]
        Org result = Org.create(params)

        then:
        result != null
        result.name == "test"
        result.num == "test"
        result.member != null
        result.member.branch.id == branch.id

    }

    void "test delete is cascaded"() {
        setup:
        emptyDimensions()

        Org org = createOrg("test", "T001", OrgType.Branch).persist(flush: true)
        Long oid = org.id
        OrgMember orgMember = OrgMember.make(org)
        orgMember.persist(flush: true)
        org.member = orgMember
        org.persist()
        flushAndClear()

        when:
        Org.get(oid).delete(flush:true)

        then:
        Org.get(oid) == null
        OrgMember.get(orgMember.id) == null
    }

    def "constraint: nothing is required when no dimensions specified"() {
        when:
        emptyDimensions()
        Org org = createOrg("test", "T001", OrgType.Branch)
        OrgMember orgMember = OrgMember.make(org)

        then:
        orgMember.validate() == true
        !orgMember.hasErrors()
    }

    def "fails when no member fields are specified but parents required"() {
        when:
        OrgDimensionTesting.setDimensions([OrgType.CustAccount, OrgType.Customer, OrgType.Branch])

        Org org = Org.create(name:"test", num:"T001", orgTypeId:OrgType.Customer.id)

        then: "Branch should be required for customer"
        DataProblemException ex = thrown()
        ex.entity != null
        ex.entity instanceof Org

        when:
        org = ex.entity as Org

        then:
        org.validate() == false
        org.member != null
        org.hasErrors()
        validateErrors(org.errors, ["member.branch"])

        when:
        org = Org.create(name:"test", num:"T001", orgTypeId:OrgType.Branch.id)

        then: "Should not require any thing for branch"
        noExceptionThrown()
        org != null
        org.validate()
        org.hasErrors() == false

    }

    @Unroll
    def "test constraints: fields #requiredLevels should be required for paths #dimension and orgType #orgType"(OrgType orgType, List dimension, List requiredLevels) {
        setup:
        OrgDimensionTesting.setDimensions(dimension)

        when:
        Org org = createOrg("Test", "T001", orgType)
        OrgMember member = OrgMember.make(org)

        then:
        !member.validate()
        member.hasErrors() == true
        validateErrors(member.errors, requiredLevels)

        where:

        orgType          | dimension                          | requiredLevels
        OrgType.Branch   | ['Branch', 'Division']             | ["division"]
        OrgType.Branch   | ['Branch', 'Division', 'Business'] | ["division", "business"]
        OrgType.Division | ['Branch', 'Division', 'Business'] | ["business"]

    }

    @Unroll
    def "test 2 dims: dim1 #dimension, dim2 #dimension2 and orgType #orgType"(OrgType orgType, List dimension, List dimension2, List requiredLevels) {
        setup:
        OrgDimensionTesting.setDimensions(dimension, dimension2)

        when:
        Org org = createOrg("Test", "T001", orgType)
        OrgMember member = OrgMember.make(org)

        then:
        !member.validate()
        member.hasErrors() == true
        validateErrors(member.errors, requiredLevels)

        where:

        orgType             | dimension                             | dimension2                         | requiredLevels
        OrgType.Customer    | ['Customer', 'Branch']                | ['Branch', 'Division']             | ["branch", "division"]
        OrgType.CustAccount | ['CustAccount', 'Customer', 'Branch'] | ['Branch', 'Division', 'Business'] | ["branch", "division", "business"]
        // OrgType.Division    | ["Branch.Division", "Division.Business", "Business.Region"] | ["business", "region"]

    }

    private void validateErrors(Errors errors, List requiredLevels) {
        List all = ["branch", "division", "business", "sales", "region", "factory"]

        requiredLevels.each { String level ->
            assert errors.hasFieldErrors(level)
            assert errors.getFieldError(level).code == "NotNull"
        }

        List notRequired = all - requiredLevels
        notRequired.each { String level ->
            assert !errors.hasFieldErrors(level)
        }

    }

    private Org createOrg(String name, String num, OrgType orgType) {
        Org org = Org.of(num, name, orgType).persist()
        return org
    }
}
