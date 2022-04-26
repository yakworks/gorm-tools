package yakworks.rally

import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecRole
import gorm.tools.security.domain.SecRoleUser
import gorm.tools.testing.TestDataJson
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Specification
import yakworks.commons.map.Maps
import yakworks.gorm.testing.SecurityTest
import yakworks.rally.listeners.RallyEventListener

class RallyEventListenerSpec extends Specification implements DataRepoTest, SecurityTest {

    void setupSpec() {
        defineBeans({
            rallyEventListener(RallyEventListener) { bean ->
                bean.lazyInit = true
                bean.autowire = "byType"
            }
        })
        mockDomains AppUser, SecRole, SecRoleUser
    }

    void "test orgid assignment"() {
        setup:
        Map userProps = [email: "email@9ci.com", username: "test-user-1", save:false]
        Map args = TestDataJson.buildMap(Maps.clone(userProps), AppUser)
        args.orgId = 100
        AppUser loggedInUser = AppUser.create(args)

        expect:
        loggedInUser.id == 1
        loggedInUser.orgId == 100

        when: "assign logged in user orgid"
        args = TestDataJson.buildMap(Maps.clone(userProps), AppUser)
        AppUser user = AppUser.create(args)

        then:
        user.id == 2
        user.orgId != null
        user.orgId == loggedInUser.orgId
        user.orgId == 100

        when: "logged in orgid is null"
        loggedInUser.orgId = null
        loggedInUser.save()
        user = AppUser.create(TestDataJson.buildMap(Maps.clone(userProps), AppUser))

        then:
        user.orgId != null
        user.orgId == 2 //default
    }

}
