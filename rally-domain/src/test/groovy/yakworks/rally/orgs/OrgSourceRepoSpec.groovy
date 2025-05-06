package yakworks.rally.orgs

import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.PartitionOrg
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class OrgSourceRepoSpec extends Specification implements GormHibernateTest, SecurityTest {
    static entityClasses = [Activity, Org, OrgSource, PartitionOrg]
    static List springBeans = [OrgProps]

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

    void "test find org by sourceid created from num"() {
        when:
        Org org = Org.of("foo", "bar", OrgType.Customer)
        Org.repo.createSource(org)
        org.persist()
        flush()

        then: "source id is the default"
        assert org.source.sourceId == "foo"

        OrgSource os = OrgSource.findBySourceIdAndOrgType('foo', OrgType.Customer)
        assert os
    }

}
