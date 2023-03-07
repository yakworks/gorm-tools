package yakworks.rally.mango

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.mango.MangoBuilder
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.IgnoreRest
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

    def "mangoFindBy"() {
        expect:
        OrgMangoBench.mangoOrgFindBy()
    }

    //@IgnoreRest
    def "mangoBuilder"() {
        setup:
        OrgMangoBench orgMangoBench = new OrgMangoBench(mangoBuilder: mangoBuilder)
        expect:
        orgMangoBench.mangoBuilder()
    }

    // @IgnoreRest
    def "tidyMap"() {
        expect:
        OrgMangoBench.tidyMap()
    }

    //@IgnoreRest
    def "mangoOrgQuery"() {
        expect:
        OrgMangoBench.mangoOrgQuery()
    }

    def "mangoOrgGet"() {
        expect:
        OrgMangoBench.mangoOrgGet()
    }

}
