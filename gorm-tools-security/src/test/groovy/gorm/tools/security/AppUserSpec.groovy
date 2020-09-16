package gorm.tools.security

import org.apache.commons.lang.RandomStringUtils

import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.security.domain.SecRole
import gorm.tools.security.domain.SecRoleUser
import gorm.tools.security.domain.AppUser
import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.TestDataJson
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification

class AppUserSpec extends Specification implements DomainRepoTest<AppUser>, SecurityTest {

    // Closure doWithConfig() {
    //     return { cfg ->
    //         cfg.gorm.tools.mango.criteriaKeyName = "testCriteriaName"
    //     }
    // }
    // List<Class> getDomainClasses() { [AppUser, SecRole, SecRoleUser] }
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
        entity = new AppUser()
        args = buildMap(args)
        args << [password:'secretStuff', repassword:'secretStuff']
        entity = AppUser.create(args)
        //We have to add 'password' field manually, because it has the "bindable: false" constraint
        entity.password = 'test_pass_123'
        entity.persist(flush: true)

        get(entity.id)
    }

    @Override
    AppUser persistEntity(Map args){
        args.get('save', false) //adds save:false if it doesn't exists
        args['password'] = "test"
        entity = build(buildMap(args))
        assert entity.persist(flush: true)
        return get(entity.id)
    }

    void "did it get the audit stamp fields"() {
        when:
        def con = build()
        con.validate()

        def conProps = AppUser.constrainedProperties
        then:
        //sanity check the main ones
        conProps.username.nullable == false
        conProps['passwordHash'].property.metaConstraints["bindable"] == false
        conProps.passwordHash.display == false
        conProps['passwordHash'].property.display == false

        conProps['editedBy'].property.metaConstraints["bindable"] == false
        conProps['editedBy'].property.metaConstraints["description"] == "edited by user id"

        ['editedBy','createdBy', 'editedDate','createdDate'].each {
            assert con.hasProperty(it)
            def conProp = conProps[it].property
            conProp.metaConstraints["bindable"] == false
            assert conProp.nullable == false
            assert !conProp.editable
        }

    }

    def "test update fail"() {
        when:
        AppUser user = createEntity()
        Map params = [id: user.id, username: null]
        AppUser.update(params)

        then:
        thrown EntityValidationException
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
