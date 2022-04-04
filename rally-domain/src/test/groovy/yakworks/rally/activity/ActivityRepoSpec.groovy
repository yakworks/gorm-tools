package yakworks.rally.activity

import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.gorm.testing.SecurityTest
import yakworks.rally.activity.model.*
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource

class ActivityRepoSpec extends Specification implements DomainRepoTest<Activity>, SecurityTest {

    def setupSpec() {
        mockDomains(Org, OrgSource)
    }

    ActivityRepo activityRepo

    @Ignore //FIXME String-based queries like [executeQuery] are currently not supported in this implementation of GORM. Use criteria instead.
    void "test create activity lookup org by sourceId"() {
        when:
        Org org = build(Org)
        org.persist()
        Map params = [
            'name': "test",
            'org': [sourceId: 'test']
        ]

        activityRepo.create(params)
        flush()
        Activity activity = Activity.findByName("test")

        then:
        activity.org.num == org.num
    }
}
