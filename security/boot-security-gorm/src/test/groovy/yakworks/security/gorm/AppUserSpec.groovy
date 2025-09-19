package yakworks.security.gorm

import org.apache.commons.lang3.RandomStringUtils

import gorm.tools.problem.ValidationProblem
import gorm.tools.utils.GormMetaUtils
import spock.lang.Specification
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecPasswordHistory
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class AppUserSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [AppUser, SecRole, SecRoleUser, SecPasswordHistory]
    //static List springBeans = [PasswordConfig]

    String genRandomEmail(){
        String ename = RandomStringUtils.randomAlphabetic(10)
        return "${ename}@baz.com"
    }

    // @Override
    Map buildMap(Map args) {
        args.get('save', false)
        args.email = genRandomEmail()
        args.name = "test-user-${System.currentTimeMillis()}"
        args.username = "some_login_123"
        args
    }

    void createRoles(){

        new SecRole(id:1, code: 'admin').persist()
        new SecRole(id:2, code: 'user').persist()
        new SecRole(id:3, code: 'cust').persist()
        flush()

        assert SecRole.get(1) != null
        assert SecRole.get(1).code == "ADMIN"

        assert SecRole.get(2) != null
        assert SecRole.get(2).code == "USER"

        assert SecRole.get(3) != null
        assert SecRole.get(3).code == "CUST"
    }

    def setupSpec(){
        SecRole.withTransaction {
            createRoles()
        }
    }
    //
    // @Override
    // AppUser createEntity(Map args){
    //     //def entity = new AppUser()
    //     args = buildMap(args)
    //     args << [password:'secretStuff', repassword:'secretStuff']
    //     def entity = AppUser.create(args)
    //     //We have to add 'password' field manually, because it has the "bindable: false" constraint
    //     entity.password = 'test_pass_123'
    //     entity.persist(flush: true)
    //
    //     get(entity.id)
    // }
    //
    // @Override
    // AppUser persistEntity(Map args){
    //     args.get('save', false) //adds save:false if it doesn't exists
    //     args['password'] = "test"
    //     def entity = build(buildMap(args))
    //     assert entity.persist(flush: true)
    //     return get(entity.id)
    // }

    def "create user"(){
        when:
        AppUser user = new AppUser(id: 1, orgId: 1, username: "admin", email: "admin@9ci.com", password: "123Foo")
        user.persist(flush: true)

        then:
        user.id == 1
        user.passwordHash
        user.passwordChangedDate
    }

    void "did it get the audit stamp fields"() {
        when:
        def con = build(AppUser)
        con.validate()

        Map conProps = GormMetaUtils.findConstrainedProperties(AppUser)

        then:
        //sanity check the main ones
        conProps.username.nullable == false
        conProps['passwordHash'].metaConstraints["bindable"] == false
        conProps.passwordHash.display == false
        conProps['passwordHash'].display == false

        conProps['editedBy'].metaConstraints["bindable"] == false
        conProps['editedBy'].metaConstraints["description"] == "edited by user id"

        ['editedBy','createdBy', 'editedDate','createdDate'].each {
            assert con.hasProperty(it)
            def conProp = conProps[it]
            conProp.metaConstraints["bindable"] == false
            assert conProp.nullable == false
            assert !conProp.editable
        }

    }

    void "simple persist"() {
        when:
        Map data = buildMap([password:"test"])
        AppUser user = AppUser.create(data)
        user.persist(flush: true)

        then:
        user.editedBy == 1
        user.editedDate
        user.passwordHash
    }

    def "test update fail"() {
        when:
        AppUser user = build(AppUser)
        Map params = [id: user.id, username: null]
        AppUser.repo.update(params)

        then:
        thrown ValidationProblem.Exception
    }


    def "insert with roles"() {
        when:
        Map data = buildMap([:])
        data.roles = ["ADMIN", "USER"]
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


    def "insert with role IDS"() {
        when:
        Map data = buildMap([:])
        data.roles = [[id:1], [id:2]]
        AppUser user = AppUser.create(data)
        flush()

        then:
        user != null
        SecRoleUser.findAllByUser(user)*.role.id == [1L, 2L]
        user.roles.size() == 2
        user.roles[0] instanceof String
        user.secRoles.size() == 2
        user.secRoles[0] instanceof SecRole
    }

    def "update roles"() {
        when:
        Map data = buildMap([:])
        data.roles = [[id:1], [id:2]]
        AppUser user = AppUser.create(data)
        flushAndClear()

        Map updateParams = [
            id:user.id,
            roles: [[id: 2], [id: 3]]
        ]
        //update it to 2 and 3.
        AppUser.repo.update(updateParams)
        flushAndClear()

        then:
        user.roles.size() == 2
        SecRoleUser.findAllByUser(user)*.role.id == [2L, 3L]

    }

    def "test username"() {
        when:
        Map data = buildMap([:])
        data << [username:'jimmy', password:'secretStuff', repassword:'secretStuff']
        AppUser user = AppUser.create(data)
        flush()

        then:
        user.username == "jimmy"
        user.name.startsWith("test")

    }

    def "test displayName"() {
        when:
        Map data = buildMap([:])
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
