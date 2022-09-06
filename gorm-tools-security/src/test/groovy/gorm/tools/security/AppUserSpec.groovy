package gorm.tools.security

import org.apache.commons.lang3.RandomStringUtils

import gorm.tools.problem.ValidationProblem
import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecRole
import gorm.tools.security.domain.SecRoleUser
import gorm.tools.utils.GormMetaUtils
import yakworks.gorm.testing.SecurityTest
import gorm.tools.testing.TestDataJson
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification

class AppUserSpec extends Specification implements DomainRepoTest<AppUser>, SecurityTest {

    void setupSpec() {
        mockDomains AppUser, SecRole, SecRoleUser
    }

    String genRandomEmail(){
        String ename = RandomStringUtils.randomAlphabetic(10)
        return "${ename}@baz.com"
    }

    @Override
    Map buildMap(Map args) {
        args.get('save', false)
        args.email = genRandomEmail()
        args.name = "test-user-${System.currentTimeMillis()}"
        args.username = "some_login_123"
        args
    }

    @Override
    AppUser createEntity(Map args){
        //def entity = new AppUser()
        args = buildMap(args)
        args << [password:'secretStuff', repassword:'secretStuff']
        def entity = AppUser.create(args)
        //We have to add 'password' field manually, because it has the "bindable: false" constraint
        entity.password = 'test_pass_123'
        entity.persist(flush: true)

        get(entity.id)
    }

    @Override
    AppUser persistEntity(Map args){
        args.get('save', false) //adds save:false if it doesn't exists
        args['password'] = "test"
        def entity = build(buildMap(args))
        assert entity.persist(flush: true)
        return get(entity.id)
    }

    void "did it get the audit stamp fields"() {
        when:
        def con = build()
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
        def con = build()
        con.persist(flush: true)

        then:
        con.editedBy == 1
        con.editedDate

    }

    def "test update fail"() {
        when:
        AppUser user = createEntity()
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
        Map data = buildMap([:])
        data.roles = ["1", "2"]
        data << [password:'secretStuff', repassword:'secretStuff']
        AppUser user = AppUser.create(data)
        flush()

        then:
        user != null
        SecRoleUser.count() == 2
        SecRoleUser.findAllByUser(user)*.role.id == [1L, 2L]
        user.getRoles().size() == 2

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
        data = [ email: 'jimmy@foo.com']
        AppUser user2 = AppUser.create(data)
        flush()

        then:
        user2.displayName == "jimmy"
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
        data = [ username: 'sally', email: 'jimmy@foo.com' ]
        user = AppUser.create(data)
        flush()

        then:
        user.email == 'jimmy@foo.com'
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
