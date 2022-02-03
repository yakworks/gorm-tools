package yakworks.rally.orgs.members


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.OrgMemberService
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import spock.lang.Specification

@Integration
@Rollback
class OrgMemberServiceSpec extends Specification implements DomainIntTest {

    OrgDimensionService orgDimensionService
    OrgMemberService orgMemberService

    void initOrgDimensions(Map dims){
        orgDimensionService.dimensionsConfig = dims
        orgDimensionService.clearCache()
        orgDimensionService.init()
    }

    void testSetParent() {
        setup:
        Org customer = Org.of("T1", "T1", OrgType.Customer).persist()
        Org branch = Org.of("T2", "T2", OrgType.Branch).persist()
        Org division = Org.of("T3", "T3", OrgType.Division).persist()
        Org business = Org.of("T4", "T4", OrgType.Business).persist()
        Org sales = Org.of("T5", "T5", OrgType.Sales).persist()
        Org region = Org.of("T6", "T6", OrgType.Region).persist()
        Org factory = Org.of("T7", "T7", OrgType.Factory).persist()

        OrgMember orgMember = OrgMember.make(branch)
        orgMember.bind([division:division, business:business, sales:sales, region:region, factory: factory])
        orgMember.persist()
        branch.member = orgMember
        branch.persist(flush: true)

        when:
        orgMemberService.setupMember(customer, branch, false)

        then:
        customer.member != null
        customer.member.org == customer
        customer.member.branch == branch
        customer.member.division == division
        customer.member.business == business
        customer.member.sales == sales
        customer.member.region == region
        customer.member.factory == factory
    }

    void "test setupMember"() {
        setup:
        //appConfig.orgDimensions = [primary: "Branch.Division.Business", second: "Branch.Sales"]
        initOrgDimensions([primary: "Branch.Division.Business", second: "Branch.Sales"])

        Org division = Org.of("Division", "Division", OrgType.Division).persist()
        division.member = OrgMember.make(division)
        division.member.business = Org.of("Business", "Business", OrgType.Business).persist()
        division.persist()
        division.member.persist()
        Org sales = Org.of("Sales", "Sales", OrgType.Sales).persist()

        when:
        Org branch = Org.of("Branch", "Branch", OrgType.Branch).persist()
        orgMemberService.setupMember(branch, [division:[id:division.id], sales:[id:sales.id]])

        then:
        branch.member != null
        branch.member.org == branch
        branch.member.division == division
        branch.member.business == division.member.business
        branch.member.sales == sales

        cleanup:
        orgDimensionService.testInit(null)
    }

    void "test setupMember lookup by num"() {
        setup:
        initOrgDimensions([primary: "Branch.Division"])
        Org division = Org.findByOrgTypeId(OrgType.Division.id)

        when:
        Org branch = Org.of("Branch", "Branch", OrgType.Branch).persist()
        orgMemberService.setupMember(branch, [division:[num:division.num]])

        then:
        branch.member != null
        branch.member.org == branch
        branch.member.division == division

        cleanup:
        orgDimensionService.testInit(null)
    }

    void "test setupMember lookup for customer by org source "() {
        setup:
        //sourceId is assigned from num
        Org customer = Org.of("T1", "T1", OrgType.Customer).persist()
        Org branch = Org.of("B2", "B2", OrgType.Branch).persist()
        Org.repo.createSource(customer)
        customer.persist(flush:true)
        assert OrgSource.repo.findOrgIdBySourceIdAndOrgType("T1" as String, OrgType.get(1))

        // orgMemberService.setupMember(customer, [branch:[num:branch.num]])

        initOrgDimensions([primary: "CustAccount.Customer"])
        when:
        Org custAccount = Org.of("test", "test", OrgType.CustAccount).persist()
        orgMemberService.setupMember(custAccount, [customer:[org:[source:[sourceId:'T1']]]])

        then:
        custAccount

        cleanup:
        orgDimensionService.testInit(null)
    }
}
