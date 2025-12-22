package yakworks.security.spring

import org.apache.commons.lang3.RandomStringUtils

import spock.lang.Specification
import yakworks.security.Roles
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.security.spring.user.SpringUser
import yakworks.security.user.BasicUserInfo
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class SpringUserSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [AppUser, SecRole, SecRoleUser]

    String genRandomEmail(){
        String ename = RandomStringUtils.randomAlphabetic(10)
        return "${ename}@baz.com"
    }

    // @Override
    Map userMap(Map args = [:]) {
        args.email = genRandomEmail()
        args.name = "Joe ${System.currentTimeMillis()}"
        args.username = "test-user-${System.currentTimeMillis()}"
        args
    }

    void "create SpringUser with roles"() {
        when:
        def uinfo = BasicUserInfo.create(id: 1, username: "admin@yak.com", email: "admin2@y.com", roles: ['ROLE1'])
        def springUser = SpringUser.of(uinfo, [Roles.ADMIN, Roles.CUSTOMER])

        then:
        springUser.roles ==  [Roles.ADMIN, Roles.CUSTOMER] as Set
    }

    void "create SpringUser from AppUser"() {
        when:
        def appUser = new AppUser(userMap(id:1))
        assert appUser.validate()
        def springUser = SpringUser.of(appUser)

        then:
        springUser.username.startsWith("test-user-")
        ['id', 'username', 'name', 'displayName', 'email', 'orgId'].each{
            assert springUser[it] == appUser[it]
        }
    }

}
