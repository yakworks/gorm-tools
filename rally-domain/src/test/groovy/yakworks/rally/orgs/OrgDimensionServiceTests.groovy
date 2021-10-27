package yakworks.rally.orgs

import org.apache.commons.lang3.EnumUtils

import yakworks.gorm.testing.SecurityTest
import gorm.tools.testing.unit.DataRepoTest
import grails.testing.services.ServiceUnitTest
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.OrgTypeSetup
import spock.lang.Specification

class OrgDimensionServiceTests extends Specification implements ServiceUnitTest<OrgDimensionService>, DataRepoTest, SecurityTest {

    void setupSpec() {
        mockDomain OrgTypeSetup
    }

    void initPaths(){
        service.parsePathsAndInitCache([
            "CustAccount.Customer.Division.Business",
            "CustAccount.Branch.Division.Business"
        ])
    }

    void "test CustAccount"() {
        expect:
        initPaths()
        verifyParents("CustAccount", ["Customer", "Branch", "Division", "Business"])
        verifyImmediatedParents("CustAccount", ["Customer", "Branch"])
        verifyChilds("CustAccount", [])
    }

    void "test Division"() {
        expect:
        initPaths()
        verifyChilds("Division", ["Customer", "Branch", "CustAccount"])
        verifyParents("Division", ["Business"])
        verifyImmediatedParents("Division", ["Business"])
    }

    void "test client and company"() {
        when:
        initPaths()
        List allOrgTypes = ['Customer', 'CustAccount', 'Branch', 'Division', 'Business']

        then:
        allOrgTypes

        verifyChilds("Client", allOrgTypes)
        verifyParents("Client", [])
        verifyImmediatedParents("Client", [])

        verifyImmediatedParents("Company", [])
        verifyChilds("Company", allOrgTypes)
        verifyParents("Company", [])

    }

    void "test imutable"() {

        when: "User modifies the returned list"
        initPaths()
        List childs = service.getChildLevels(OrgType.Division)
        List parents = service.getParentLevels(OrgType.Division)
        childs.add(OrgType.Prospect)
        parents.add(OrgType.Prospect)

        then: "Cache should not have been modified, and it should continue returning the same results"
        verifyChilds("Division", ["Customer", "Branch", "CustAccount"])
        verifyParents("Division", ["Business"])
        verifyImmediatedParents("Division", ["Business"])
    }

    private void verifyChilds(String name, List<String> expected) {
        List<OrgType> childs = service.getChildLevels(getEnum(name))
        verifyCommon(childs, expected)
    }

    private void verifyParents(String name, List<String> expected) {
        List<OrgType> parents = service.getParentLevels(getEnum(name))
        verifyCommon(parents, expected)
    }

    private void verifyImmediatedParents(String name, List<String> expected) {
        List<OrgType> parents = service.getImmediateParents(getEnum(name))
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
