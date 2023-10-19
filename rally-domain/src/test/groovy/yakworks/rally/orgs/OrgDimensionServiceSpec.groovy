package yakworks.rally.orgs

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.commons.lang.EnumUtils
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.testing.OrgDimensionTesting
import yakworks.spring.AppCtx
import yakworks.testing.grails.GrailsAppUnitTest

class OrgDimensionServiceSpec extends Specification implements GrailsAppUnitTest {

    @Autowired OrgDimensionService orgDimensionService

    Closure doWithSpring() { { ->
        orgDimensionService(OrgDimensionService)
        orgProps(OrgProps)
        appCtx(AppCtx)
    }}

    def setup(){
        OrgDimensionTesting.setDimensions(
            ['CustAccount', 'Customer', 'Division', 'Business'],
            ['CustAccount', 'Branch', 'Division', 'Business']
        )
    }

    void "test CustAccount"() {
        expect:
        verifyParents("CustAccount", ["Customer", "Branch", "Division", "Business"])
        verifyImmediatedParents("CustAccount", ["Customer", "Branch"])
        verifyChilds("CustAccount", [])
    }

    void "test Division"() {
        expect:
        verifyChilds("Division", ["Customer", "Branch", "CustAccount"])
        verifyParents("Division", ["Business"])
        verifyImmediatedParents("Division", ["Business"])
    }

    void "test client and company"() {
        when:
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
        List childs = orgDimensionService.getChildLevels(OrgType.Division)
        List parents = orgDimensionService.getParentLevels(OrgType.Division)
        childs.add(OrgType.Prospect)
        parents.add(OrgType.Prospect)

        then: "Cache should not have been modified, and it should continue returning the same results"
        verifyChilds("Division", ["Customer", "Branch", "CustAccount"])
        verifyParents("Division", ["Business"])
        verifyImmediatedParents("Division", ["Business"])
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
