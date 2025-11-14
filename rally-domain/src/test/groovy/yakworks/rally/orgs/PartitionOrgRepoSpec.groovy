package yakworks.rally.orgs

import spock.lang.Specification
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.PartitionOrg
import yakworks.rally.orgs.repo.PartitionOrgRepo
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

import javax.inject.Inject

class PartitionOrgRepoSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [Org, OrgTag, OrgType, PartitionOrg]
    static List springBeans = [OrgProps]

    @Inject PartitionOrgRepo repo
    @Inject OrgProps orgProps

    void setup() {
        orgProps.partition.type = OrgType.Company
    }


    void "create and remove"() {
        when:
        Org org = Org.of("foo", "bar", OrgType.Company).persist()
        flush()

        then:
        PartitionOrg.repo.exists(org.id)

        when: "removeForOrg"
        Org.repo.removeById(org.id)
        flush()

        then:
        !PartitionOrg.exists(org.id)
    }

    void "lookup"() {
        setup:
        Org org = Org.of("foo", "bar", OrgType.Company).persist()
        flush()

        expect:
        PartitionOrg.repo.exists(org.id)

        when: "by num"
        PartitionOrg porg = repo.findWithData([num: 'foo'])

        then:
        porg
        porg.id == org.id

        when: "by id"
        porg = repo.findWithData([id: org.id])

        then:
        porg
        porg.id == org.id
    }
}
