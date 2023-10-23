package yakworks.rally.orgs.members


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import yakworks.rally.testing.OrgDimensionTesting
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.orgs.OrgService
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import spock.lang.Specification

@Integration
@Rollback
class OrgMemberServiceSpec extends Specification implements DomainIntTest {

    OrgService OrgService

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
        OrgService.setupMember(customer, branch, false)

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
        OrgDimensionTesting.setDimensions(['Branch','Division','Business'], ['Branch','Sales'])

        Org division = Org.of("Division", "Division", OrgType.Division).persist()
        division.member = OrgMember.make(division)
        division.member.business = Org.of("Business", "Business", OrgType.Business).persist()
        division.persist()
        division.member.persist()
        Org sales = Org.of("Sales", "Sales", OrgType.Sales).persist()

        when:
        Org branch = Org.of("Branch", "Branch", OrgType.Branch).persist()
        OrgService.setupMember(branch, [division:[id:division.id], sales:[id:sales.id]])

        then:
        branch.member != null
        branch.member.org == branch
        branch.member.division == division
        branch.member.business == division.member.business
        branch.member.sales == sales

        cleanup:
        OrgDimensionTesting.resetDimensions()
    }

    void "test setupMember lookup by num"() {
        setup:
        OrgDimensionTesting.setDimensions(['Branch','Division'])
        Org division = Org.findByOrgTypeId(OrgType.Division.id)
        //setup another org with same num to make sure its locating the right one
        Org dupDiv = Org.of(division.num, division.num + "dup", OrgType.Branch).persist(flush:true)

        when:
        Org branch = Org.of("Branch", "Branch", OrgType.Branch).persist()
        OrgService.setupMember(branch, [division:[num:division.num]])

        then:
        branch.member != null
        branch.member.org == branch
        branch.member.division == division

        cleanup:
        OrgDimensionTesting.resetDimensions()
    }

    void "test setupMember lookup for customer by org source "() {
        setup:
        //sourceId is assigned from num
        Org customer = Org.of("T1", "T1", OrgType.Customer).persist()
        Org branch = Org.of("B2", "B2", OrgType.Branch).persist()
        Org.repo.createSource(customer)
        customer.persist(flush:true)
        assert OrgSource.repo.findOrgIdBySourceIdAndOrgType("T1" as String, OrgType.get(1))

        when:
        Org custAccount = Org.of("test", "test", OrgType.CustAccount).persist()
        OrgService.setupMember(custAccount, [customer:[org:[source:[sourceId:'T1']]]])

        then:
        custAccount

       //  cleanup:
       // initOrgDimensions(null)
    }
}
