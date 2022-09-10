package yakworks.rally.orgs.members

import yakworks.commons.lang.EnumUtils

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import yakworks.testing.gorm.DomainIntTest
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.model.OrgType
import spock.lang.Specification

@Integration
@Rollback
class OrgDimensionServiceTests extends Specification implements DomainIntTest {

    OrgDimensionService orgDimensionService
    /*
    example from CED:
    dimensions {
        org {
            primary {
                path = "CustAccount.Branch.Division" //.Business"
            }
            custDim {
                path = "CustAccount.Customer.Division" //.Business"
            }
        }
    }
    */
    void setupDims() {
        orgDimensionService.dimensionsConfig = [
            test1: "CustAccount.Branch.Division.Business",
            test2: "CustAccount.Branch.Sales.Region",
            test3: "CustAccount.Customer.Division.Business"
        ]

        //repopulate cache based on the new config specified above.
        orgDimensionService.init()
    }
    def cleanup(){
        orgDimensionService.testInit(null)
    }

    def "dimensions are empty"() {
        when:
        orgDimensionService.testInit(null)

        then:
        orgDimensionService.getChildLevels(OrgType.Business).size() == 0
        orgDimensionService.getParentLevels(OrgType.Business).size() == 0
        orgDimensionService.getImmediateParents(OrgType.Business).size() == 0
    }

    def "test dimension levels"() {
        when:
        setupDims()

        then:

        orgDimensionService.getAllLevels().size() == 7
        orgDimensionService.getAllLevels()*.name().containsAll(["CustAccount", "Customer", "Branch", "Division", "Business", "Sales", "Region"])

        verifyChilds("Business", ["Division", "Branch", "CustAccount", "Customer"])
        verifyParents("Business", [])
        verifyImmediatedParents("Business", [])

        verifyChilds("Region", ["Sales", "Branch", "CustAccount"])
        verifyImmediatedParents("Region", [])
        verifyParents("Region", [])

        verifyChilds("Division", ["Customer", "Branch", "CustAccount"])
        verifyParents("Division", ["Business"])
        verifyImmediatedParents("Division", ["Business"])

        verifyChilds("Sales", ["Branch", "CustAccount"])
        verifyParents("Sales", ["Region"])
        verifyImmediatedParents("Sales", ["Region"])

        verifyImmediatedParents("Branch", ["Division", "Sales"])
        verifyChilds("Branch", ["CustAccount"])
        verifyParents("Branch", ["Business", "Division", "Sales", "Region"])


        verifyChilds("Customer", ["CustAccount"])
        verifyParents("Customer", ["Business", "Division"])
        verifyImmediatedParents("Customer", ["Division"])

        verifyParents("CustAccount", ["Business", "Division", "Branch", "Customer", "Sales", "Region"])
        verifyChilds("CustAccount", [])
        verifyImmediatedParents("CustAccount", ["Branch", "Customer"])

    }

    def "test company and client"() {
        when: "dimensions are specified"
        setupDims()
        // List allOrgTypes = OrgType.query {
        //     not {
        //         inList("id", [OrgTypeEnum.Client.id, OrgTypeEnum.Company.id])
        //     }
        // }.list().collect { it.orgTypeEnum.name() }

        List allOrgTypes = orgDimensionService.allLevels
        def childs = orgDimensionService.getChildLevels(OrgType.Client)

        then:
        allOrgTypes
        childs.size() == allOrgTypes.size()
        childs.containsAll(allOrgTypes)
        //no parents
        orgDimensionService.getParentLevels(OrgType.Client) == []

        when:
        childs = orgDimensionService.getChildLevels(OrgType.Company)

        then:
        childs.size() == allOrgTypes.size()
        childs.containsAll(allOrgTypes)
        //no parents
        orgDimensionService.getParentLevels(OrgType.Company) == []

        cleanup:
        // clearAppConfigCache()
        orgDimensionService.clearCache()
        orgDimensionService.testInit(null)

    }

    private void verifyChilds(String name, List<String> expected) {
        List<OrgType> childs = orgDimensionService.getChildLevels(getEnum(name))
        verifyCommon(childs, expected)
    }

    private void verifyParents(String name, List<String> expected) {
        List<OrgType> parents = orgDimensionService.getParentLevels(getEnum(name))
        verifyCommon(parents, expected)
    }

    private void verifyImmediatedParents(String name, List<String> expected) {
        List<OrgType> parents = orgDimensionService.getImmediateParents(getEnum(name))
        verifyCommon(parents, expected)
    }

    private void verifyCommon(List<OrgType> list, List<String> expected) {
        assert list.size() == expected.size()
        assert list.containsAll(getEnums(expected))
    }

    private OrgType getEnum(String name){
        EnumUtils.getEnum(OrgType, name)
    }
    private List<OrgType> getEnums(List<String> expected){
        expected.collect{ EnumUtils.getEnum(OrgType, it)}
    }

}
