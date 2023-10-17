package yakworks.rally.orgs.members

import yakworks.commons.lang.EnumUtils

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import yakworks.rally.config.OrgProps
import yakworks.rally.testing.OrgDimensionTesting
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.model.OrgType
import spock.lang.Specification

@Integration
@Rollback
class OrgDimensionServiceTests extends Specification implements DomainIntTest {

    OrgDimensionService orgDimensionService
    OrgProps orgProps

    void setupDims() {
        orgProps.members.enabled = true
        orgProps.members.dimension = [OrgType.CustAccount, OrgType.Branch, OrgType.Division, OrgType.Business]
        orgProps.members.dimension2 = [OrgType.CustAccount, OrgType.Customer, OrgType.Division, OrgType.Business]
        orgDimensionService.isInitialized = false
        orgDimensionService.init()
        // orgDimensionService.setDimensions([
        //     "CustAccount.Branch.Division.Business",
        //     "CustAccount.Customer.Division.Business"
        // ]).init()
    }

    def cleanup(){
        OrgDimensionTesting.resetDimensions()
    }

    def "dimensions are empty"() {
        when:
        OrgDimensionTesting.emptyDimensions()

        then:
        orgDimensionService.getChildLevels(OrgType.Business).size() == 0
        orgDimensionService.getParentLevels(OrgType.Business).size() == 0
        orgDimensionService.getImmediateParents(OrgType.Business).size() == 0
    }

    def "test dimension levels"() {
        when:
        setupDims()

        then:

        orgDimensionService.getAllLevels().size() == 5
        orgDimensionService.getAllLevels()*.name().containsAll(["CustAccount", "Customer", "Branch", "Division", "Business"])

        verifyChilds("Business", ["Division", "Branch", "CustAccount", "Customer"])
        verifyParents("Business", [])
        verifyImmediatedParents("Business", [])

        verifyChilds("Division", ["Customer", "Branch", "CustAccount"])
        verifyParents("Division", ["Business"])
        verifyImmediatedParents("Division", ["Business"])

        verifyImmediatedParents("Branch", ["Division"])
        verifyChilds("Branch", ["CustAccount"])
        verifyParents("Branch", ["Business", "Division"])


        verifyChilds("Customer", ["CustAccount"])
        verifyParents("Customer", ["Business", "Division"])
        verifyImmediatedParents("Customer", ["Division"])

        verifyParents("CustAccount", ["Business", "Division", "Branch", "Customer"])
        verifyChilds("CustAccount", [])
        verifyImmediatedParents("CustAccount", ["Branch", "Customer"])

    }

    def "test company and client"() {
        when: "dimensions are specified"
        setupDims()

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
        OrgDimensionTesting.resetDimensions()

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
