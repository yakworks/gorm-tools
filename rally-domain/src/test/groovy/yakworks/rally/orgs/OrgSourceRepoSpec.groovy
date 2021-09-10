package yakworks.rally.orgs

import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType

class OrgSourceRepoSpec extends Specification implements DomainRepoTest<OrgSource>, SecurityTest {

    def setupSpec() {
        // defineBeans{
        //     orgDimensionService(OrgDimensionService)
        // }
        mockDomains(Org)
    }

    void "CRUD tests"() {
        expect:
        // createEntity().id
        persistEntity().id
        updateEntity().version > 0
        removeEntity()
    }

    void testInsertOrgSources(){
        when:
        Org org = build(Org)
        org.id = 202
        org.persist()
        Map params = [sourceId: '123', source: 'QB', sourceType: 'ERP', orgId: 202, orgType: OrgType.Customer]
        OrgSource.create(params)
        flushAndClear()

        OrgSource os = OrgSource.findByOrgIdAndSource(202, 'QB')

        then:
        os != null
        params.source == os.source
        os.orgId == 202

    }

    void "test findBySourceIdAndOrgType"(){
        when:
        Org org = build(Org)
        org.id = 202
        org.persist()
        Map params = [sourceId: '123', source: 'QB', sourceType: 'ERP', orgId: 202, orgType: OrgType.Customer]
        OrgSource.create(params)
        flushAndClear()

        OrgSource os = OrgSource.findBySourceIdAndOrgType('123', OrgType.Customer)

        then:
        os != null


    }

}
