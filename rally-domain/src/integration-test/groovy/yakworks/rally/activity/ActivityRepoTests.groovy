package yakworks.rally.activity

import org.springframework.jdbc.core.JdbcTemplate

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class ActivityRepoTests extends Specification implements DomainIntTest {

    ActivityRepo activityRepo
    JdbcTemplate jdbcTemplate

    void "query by linked id"() {
        when:
        //query is overriden in ActivityRepo, its the method thats called by the controller eventually
        // see controller tests as well, this is just closer to the wire
        List<Activity> acts = activityRepo.query([q:[linkedId: 10, linkedEntity:'Org'], sort:'id']).list()

        then:
        acts.size() == 1
    }

}
