package yakworks.rally.activity

import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.GormToolsHibernateSpec
import yakworks.testing.gorm.unit.DomainRepoTest
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.SecurityTest
import yakworks.rally.activity.model.*
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource

class ActivityRepoSpec extends GormToolsHibernateSpec implements SecurityTest {

    static entityClasses = [Activity, Org, OrgSource]

    ActivityRepo activityRepo

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
