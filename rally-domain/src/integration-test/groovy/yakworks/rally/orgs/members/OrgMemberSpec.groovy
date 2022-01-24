package yakworks.rally.orgs.members

import org.springframework.validation.Errors

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreRest
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.orgs.OrgDimensionService
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
    OrgDimensionService orgDimensionService

    def "test sanity check on orgMember"() {
        setup:
        //appConfig.orgDimensions = [:]
        orgDimensionService.testInit(null)
        Org org = Org.of("O1", "O1", OrgType.Customer).persist()
        Org branch = Org.of("Branch", "Branch", OrgType.Branch).persist()
        Org division = Org.of("Division", "Division", OrgType.Division).persist()

        OrgMember member = OrgMember.make(org)

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

    void "test insert with orgmembers"() {
        given:
        orgDimensionService.testInit('Branch.Division.Business')
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

        when:
        Org otherBusiness = Org.of("b2", "b2", OrgType.Business).persist([flush: true])
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

    void "test create customer with branch by id"() {
        given:
        orgDimensionService.testInit('Branch')
        orgDimensionService.dimensionsConfig = [ test1: "Customer.Branch" ]
        orgDimensionService.clearCache()
        orgDimensionService.init()
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

        cleanup:
        orgDimensionService.testInit(null)
    }

    void "test create customer with branch by num"() {
        given:
        orgDimensionService.testInit('Branch')
        orgDimensionService.dimensionsConfig = [ test1: "Customer.Branch" ]
        orgDimensionService.clearCache()
        orgDimensionService.init()
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

        cleanup:
        orgDimensionService.testInit(null)
    }

    void "test delete is cascaded"() {
        setup:
        orgDimensionService.testInit(null)
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
        orgDimensionService.testInit(null)
        Org org = createOrg("test", "T001", OrgType.Branch)
        OrgMember orgMember = OrgMember.make(org)

        then:
        orgMember.validate() == true
        !orgMember.hasErrors()
    }


    @Unroll
    def "test constraints: fields #requiredLevels should be required for paths #paths and orgType #orgType"(OrgType orgType, List paths, List requiredLevels) {
        setup:
        orgDimensionService.parsePathsAndInitCache(paths)

        when:
        Org org = createOrg("Test", "T001", orgType)
        OrgMember member = OrgMember.make(org)

        then:
        !member.validate()
        member.hasErrors() == true
        validateErrors(member.errors, requiredLevels)

        cleanup:
        orgDimensionService.testInit(null)

        where:

        orgType             | paths                                                        | requiredLevels
        OrgType.Branch      | ["Branch.Division"]                                         | ["division"]
        OrgType.Branch      | ["Branch.Division.Business"]                                | ["division", "business"]
        OrgType.Division    | ["Branch.Division.Business"]                                | ["business"]
        OrgType.Customer    | ["Customer.Branch", "Branch.Division"] | ["branch", "division"]
        OrgType.CustAccount | ["CustAccount.Customer.Branch", "Branch.Division.Business"] | ["branch", "division", "business"]
        OrgType.Division    | ["Branch.Division", "Division.Business", "Business.Region"] | ["business", "region"]
    }

    private void validateErrors(Errors errors, List requiredLevels) {
        List all = ["branch", "division", "business", "sales", "region", "factory"]

        requiredLevels.each { String level ->
            assert errors.hasFieldErrors(level)
            assert errors.getFieldError(level).code == "nullable"
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
