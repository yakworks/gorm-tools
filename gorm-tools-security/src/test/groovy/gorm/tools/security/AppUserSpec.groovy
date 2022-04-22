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
import yakworks.gorm.testing.TestingSecService

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
        args.email = genRandomEmail()
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

    void "test orgid assignment"() {
        setup:
        Map args = buildMap()
        args.orgId = 100
        AppUser loggedInUser = AppUser.create(args)

        expect:
        loggedInUser.id == 1

        when: "assign logged in user orgid"
        args = buildMap()
        AppUser user = AppUser.create(args)

        then:
        user.id == 2
        user.orgId != null
        user.orgId == loggedInUser.orgId
        user.orgId == 100

        when: "logged in orgid is null"
        loggedInUser.orgId = null
        loggedInUser.save()
        user = AppUser.create(buildMap())

        then:
        user.orgId != null
        user.orgId == 2 //default
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
        SecRole.create(TestDataJson.buildMap([save:false, name: 'ROLE_1'], SecRole))
        SecRole.create(TestDataJson.buildMap([save:false, name: 'ROLE_2'], SecRole))

        expect:
        SecRole.get(1) != null
        SecRole.get(1).name == "ROLE_1"

        SecRole.get(2) != null
        SecRole.get(2).name == "ROLE_2"

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

    def "user name"() {
        when:
        Map data = buildMap([:])
        data << [password:'secretStuff', repassword:'secretStuff']
        AppUser user = AppUser.create(data)
        flush()

        then:
        user.name.startsWith "test"
    }

    // def "statics test"() {
    //     expect:
    //     AppUser.constraints instanceof Closure
    //     AppUser.AuditStampTrait__buzz == true
    //     // def o = new Object() as AuditStampTrait
    //     // o.constraints == [foo:'bar']
    // }


}
