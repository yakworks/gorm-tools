package yakworks.rally

import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.testing.gorm.TestDataJson
import yakworks.testing.gorm.unit.DataRepoTest
import spock.lang.Specification
import yakworks.commons.map.Maps
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest
import yakworks.rally.listeners.RallyEventListener

class RallyEventListenerSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List<Class> entityClasses = [AppUser, SecRole, SecRoleUser]
    static List springBeans = [RallyEventListener]

    void "test orgid assignment"() {
        setup:
        Map userProps = [email: "email@9ci.com", username: "test-user-1",  orgId: 100]

        when: "appUser is created with an orgId"
        AppUser user = AppUser.create(userProps)

        then: "it should keep that orgId"
        user.orgId == 100

        when: "its creates with null orgId"
        user = AppUser.create([email: "email2@9ci.com", username: "test-user-2"])

        then: "should get the users"
        user.orgId == 1 //default
    }

}
