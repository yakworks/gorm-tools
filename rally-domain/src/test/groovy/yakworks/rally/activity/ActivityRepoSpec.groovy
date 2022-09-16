package yakworks.rally.activity

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.SecurityTest
import yakworks.testing.gorm.unit.GormHibernateTest

class ActivityRepoSpec extends Specification implements GormHibernateTest, SecurityTest {

    static entityClasses = [Activity, Org, OrgSource]

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
