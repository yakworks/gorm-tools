package yakworks.rally.orgs


import gorm.tools.transaction.TrxUtils
import spock.lang.Specification
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgCalc
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgInfo
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.PartitionOrg
import yakworks.rally.testing.OrgDimensionTesting
import yakworks.spring.AppCtx
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class OrgMemberSpec extends Specification implements GormHibernateTest, SecurityTest {

    static entityClasses = [Org, OrgSource, OrgTag, Location, Contact, OrgFlex, OrgCalc, OrgInfo, OrgMember, PartitionOrg]

    Closure doWithGormBeans(){ { ->
        orgDimensionService(OrgDimensionService)
        orgService(OrgService)
        orgProps(OrgProps)
    }}

    void "sanity check build"() {
        when:
        Org org = build(Org, [type: OrgType.Business])
        def orgMem = build(OrgMember, [org: org, id: org.id])

        then:
        orgMem.id

    }

    void "create Org with members"() {
        when:
        AppCtx.setApplicationContext(getApplicationContext())
        //initDimensions([OrgType.CustAccount, OrgType.Customer, OrgType.Branch, OrgType.Division, OrgType.Company])
        OrgDimensionTesting.setDimensions([OrgType.Customer, OrgType.Division, OrgType.Company])

        Org company =  Org.create(
            num: 'c1', name: 'c1', type: OrgType.Company
        )
        TrxUtils.flush()

        Org division =  Org.create(
            num: 'div1', name: 'div1', type: OrgType.Division,
            member: [company: [id: company.id]]
        )

        Org cust =  Org.create(
            num: 'div1', name: 'div1', type: OrgType.Customer,
            member: [division: [id: division.id]]
        )
        TrxUtils.flush()

        then:
        division.refresh()
        division.member.company == company
        cust.member.division == division
        cust.member.company == company
    }
}
