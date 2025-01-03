package yakworks.rally.activity

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.PartitionOrg
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class ActivityRepoSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [Activity, Org, OrgSource, PartitionOrg]
    static List springBeans = [OrgProps]

    @Autowired ActivityRepo activityRepo

    void "test create activity lookup org by num"() {
        when:
        Org org = new Org(num: 'tsla', name: 'Tesla', type: OrgType.Customer).persist()
        flush()
        Map params = [
            'name': "test",
            'org': [num: 'tsla']
        ]

        activityRepo.create(params)
        flush()
        Activity activity = Activity.findByName("test")

        then:
        activity.org.num == org.num
    }
}
