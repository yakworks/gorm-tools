package yakworks.rally.mango

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.mango.MangoBuilder
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest

//@Ignore //un-ignore to see performance diffs
@Integration
@Rollback
class OrgMangoBenchTests extends Specification implements DomainIntTest {

    @Autowired
    MangoBuilder mangoBuilder

    def "Iterate"() {
        expect:
        OrgMangoBench.forLoopBaseLine()
    }

    //This is the fastest and sets baseline
    def "get - JPQL"() {
        expect:
        OrgMangoBench.findWithHQL()
    }

    def "get - findByName"() {
        expect:
        OrgMangoBench.findByName()
    }

    def "get - findWhere"() {
        expect:
        OrgMangoBench.findWhere()
    }

    def "get - mangoOrgQuery with map"() {
        expect:
        OrgMangoBench.mangoOrgQueryGetWithMap()
    }

    def "get - mangoOrgQuery with closure"() {
        expect:
        OrgMangoBench.mangoOrgQueryGetWithClosure()
    }

    //------------ check the building of the MangoCriteria


    // @IgnoreRest
    def "tidyMap"() {
        expect:
        OrgMangoBench.tidyMap()
    }


    //@IgnoreRest
    def "mangoBuilderBuild"() {
        setup:
        OrgMangoBench orgMangoBench = new OrgMangoBench(mangoBuilder: mangoBuilder)
        expect:
        orgMangoBench.mangoBuilderBuild()
    }

    //@IgnoreRest
    def "mangoOrgQuery"() {
        //should be almost same as mangoBuilderBuild
        expect:
        OrgMangoBench.mangoOrgQuery()
    }

    def "mangoOrgQuery repo call"() {
        //should be almost same as mangoBuilderBuild
        expect:
        OrgMangoBench.mangoOrgQueryWithRepo()
    }

    def "mangoOrgQuery with closure"() {
        //builds query with closure instead of map
        expect:
        OrgMangoBench.mangoOrgQueryWithClosure()
    }


}
