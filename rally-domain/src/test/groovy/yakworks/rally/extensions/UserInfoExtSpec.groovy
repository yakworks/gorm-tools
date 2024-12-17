package yakworks.rally.extensions

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.PartitionOrg
import yakworks.security.user.CurrentUser
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class UserInfoExtSpec extends Specification implements GormHibernateTest, SecurityTest {
    static entityClasses = [Org, PartitionOrg]
    static List springBeans = [OrgProps]

    @Autowired CurrentUser currentUser

    def "isCustomer"() {
        when:
        assert !currentUser.user.isCustomer()
        new Org(id: 1, num: '123', name: 'foo', type: OrgType.Customer).persist(flush: true)
        // flushAndClear()

        then:
        currentUser.user.isCustomer()
    }

}
