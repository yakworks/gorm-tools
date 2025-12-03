package yakworks.security.gorm

import org.apache.commons.lang3.RandomStringUtils

import spock.lang.Specification
import yakworks.security.PasswordConfig
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.security.services.PasswordValidator
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class SecRoleSpec extends Specification implements GormHibernateTest, SecurityTest {

    static List entityClasses = [AppUser, SecRole, SecRoleUser]
    static List springBeans = [PasswordConfig, PasswordValidator]

    String genRandomEmail(){
        String ename = RandomStringUtils.randomAlphabetic(10)
        return "${ename}@baz.com"
    }

    AppUser createUser(){
        def entity = new AppUser(
            username: 'billy',
            email: genRandomEmail(),
            password: 'test_pass_123'
        )
        return entity.persist(flush: true)
    }

    SecRole createRole(String name){
        def entity = new SecRole(name: name)
        return entity.persist(flush: true)
    }

    void "secRoleUser create"() {
        when:
        def user = createUser()
        def role = createRole('admin')
        def sru = SecRoleUser.create(user, role, true)

        then:
        sru.user == user
        sru.role == role
    }

    // TODO do we need it?
    // void "secRoleUser create with ids"() {
    //     when:
    //     def user = createUser()
    //     def role = createRole('admin')
    //     flush()
    //     assert user.id
    //     assert role.id
    //     def sru = SecRoleUser.create([userId: user.id, roleId: role.id])
    //     flush()
    //
    //     then:
    //     sru.user == user
    //     sru.role == role
    // }

    void "secRoleUser repo create with objects"() {
        when:
        def user = createUser()
        def role = createRole('admin')
        def sru = SecRoleUser.create([user:[id: user.id], role:[id: role.id]])

        then:
        sru.user == user
        sru.role == role
    }

    void "add remove perms"() {
        setup:
        def role = createRole('admin')
        String p1 = "*:*:test"
        String p2 = "*:test:test"

        when:
        role.addPermission(p1)
        role.addPermission(p2)
        role.persist()

        then:
        noExceptionThrown()

        when:
        role = SecRole.get(role.id)

        then:
        role
        role.permissions.size() == 2
        role.hasPermission(p1)
        role.hasPermission(p2)

        when:
        role.removePermission(p1)
        role.persist()
        role = SecRole.get(role.id)

        then:
        role.permissions.size() == 1
        !role.hasPermission(p1)
    }
}
