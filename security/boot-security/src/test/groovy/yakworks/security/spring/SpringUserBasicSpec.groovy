package yakworks.security.spring


import spock.lang.Specification
import yakworks.security.Roles
import yakworks.security.spring.user.SpringUser
import yakworks.security.user.BasicUserInfo

class SpringUserBasicSpec extends Specification {

    void "create SpringUser with roles"() {
        when:
        def uinfo = BasicUserInfo.create(id: 1, username: "admin@yak.com", email: "admin2@y.com", roles: ['ROLE1'])
        def springUser = SpringUser.of(uinfo, [Roles.ADMIN, Roles.CUSTOMER])

        then:
        springUser.roles ==  [Roles.ADMIN, Roles.CUSTOMER] as Set
    }

}
