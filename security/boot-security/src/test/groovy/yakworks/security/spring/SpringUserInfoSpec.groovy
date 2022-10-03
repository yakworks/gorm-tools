package yakworks.security.spring

import org.apache.commons.lang3.RandomStringUtils

import gorm.tools.problem.ValidationProblem
import spock.lang.Specification
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.security.spring.user.SpringUser
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class SpringUserInfoSpec extends Specification implements GormHibernateTest, SecurityTest {
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


    void "simple persist"() {
        when:
        Map data = userMap([:])
        AppUser user = AppUser.create(data)
        user.persist(flush: true)

        then:
        user.editedBy == 1
        user.editedDate

    }

    def "test update fail"() {
        when:
        AppUser user = build(AppUser)
        Map params = [id: user.id, username: null]
        AppUser.update(params)

        then:
        thrown ValidationProblem.Exception
    }


    def "insert with roles"() {
        setup:
        SecRole.create(code: 'A')
        SecRole.create(code: 'B')

        expect:
        SecRole.get(1) != null
        SecRole.get(1).code == "A"

        SecRole.get(2) != null
        SecRole.get(2).code == "B"

        when:
        Map data = userMap([:])
        data.roles = ["1", "2"]
        data << [password:'secretStuff', repassword:'secretStuff']
        AppUser user = AppUser.create(data)
        flush()

        then:
        user != null
        SecRoleUser.count() == 2
        SecRoleUser.findAllByUser(user)*.role.id == [1L, 2L]
        user.getRoles().size() == 2
        user.getRoles()[0] instanceof String
        user.getSecRoles().size() == 2
        user.getSecRoles()[0] instanceof SecRole
    }

    def "test username"() {
        when:
        Map data = userMap([:])
        data << [username:'jimmy', password:'secretStuff', repassword:'secretStuff']
        AppUser user = AppUser.create(data)
        flush()

        then:
        user.username == "jimmy"
        user.name.startsWith("Joe ")

    }

    def "test displayName"() {
        when:
        Map data = userMap([:])
        data << [username:'jimmy', password:'secretStuff', repassword:'secretStuff']
        AppUser user = AppUser.create(data)
        flush()

        then:
        user.displayName == "jimmy"

        when:
        data = [email: 'jimmy2@foo.com']
        AppUser user2 = AppUser.create(data)
        flush()

        then:
        user2.displayName == "jimmy2"
    }

    def "test defaults"() {
        when: "only email is passed in"
        Map data = [ email: 'jimmy@foo.com' ]
        AppUser user = AppUser.create(data)
        flush()

        then:
        user.email == 'jimmy@foo.com'
        user.name == 'jimmy'
        //username default to?
        user.username == 'jimmy'
        user.displayName == 'jimmy'

        when: "only email and username"
        data = [ username: 'sally', email: 'jimmy2@foo.com' ]
        user = AppUser.create(data)
        flush()

        then:
        user.email == 'jimmy2@foo.com'
        user.name == 'sally'
        //username default to?
        user.username == 'sally'
        user.displayName == 'sally'

    }

    // def "statics test"() {
    //     expect:
    //     AppUser.constraints instanceof Closure
    //     AppUser.AuditStampTrait__buzz == true
    //     // def o = new Object() as AuditStampTrait
    //     // o.constraints == [foo:'bar']
    // }


}
