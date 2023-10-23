package yakworks.rally.mango

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.mango.MangoBuilder
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest

@Ignore
@Integration
@Rollback
class OrgMangoBenchTests extends Specification implements DomainIntTest {

    @Autowired
    MangoBuilder mangoBuilder

    def "Iterate"() {
        expect:
        OrgMangoBench.forLoopBaseLine()
    }

    def "findByName"() {
        expect:
        OrgMangoBench.findByName()
    }

    def "findWhere"() {
        expect:
        OrgMangoBench.findWhere()
    }

    //@IgnoreRest
    def "mangoBuilderBuild"() {
        setup:
        OrgMangoBench orgMangoBench = new OrgMangoBench(mangoBuilder: mangoBuilder)
        expect:
        orgMangoBench.mangoBuilderBuild()
    }

    // @IgnoreRest
    def "tidyMap"() {
        expect:
        OrgMangoBench.tidyMap()
    }

    //@IgnoreRest
    def "mangoOrgQuery no get"() {
        expect:
        OrgMangoBench.mangoOrgQuery()
    }

    def "mangoOrgQuery get with map"() {
        expect:
        OrgMangoBench.mangoOrgQueryGetWithMap()
    }

    def "mangoOrgQuery get with closure"() {
        expect:
        OrgMangoBench.mangoOrgQueryGetWithClosure()
    }


}
