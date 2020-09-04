package gorm.tools.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.encoding.PasswordEncoder

import gorm.tools.security.domain.SecRoleUser
import gorm.tools.security.domain.SecUser
import gorm.tools.security.domain.SecUserRepo
import gorm.tools.security.testing.SecuritySpecHelper
import gorm.tools.testing.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.IgnoreRest
import spock.lang.Specification

@Integration
@Rollback
class SecUserRepoSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {
    SecUserRepo secUserRepo
    PasswordEncoder passwordEncoder

    Map getUserParams(Map params = [:]){
        Map baseParams = [
            name:"John Galt",
            email:"test@9ci.com",
            login:'galt',
            password:'secretStuff',
            repassword:'secretStuff',
        ]
        //baseParams.putAll params
        return (baseParams << params)
    }

    def "test create"() {
        when:
        Map params = getUserParams()
        Long id = SecUser.create(params).id
        flushAndClear()

        then:
        SecUser user = SecUser.get(id)
        user.login == 'galt'
        user.email == params.email
        user.name == params.name
        passwordEncoder.isPasswordValid(user.password, params.password, null)
    }

    def "test create with roles ids"() {
        when:
        // should convert the strings to long
        Map params = getUserParams([roles: [1, "2"]])
        Long id = SecUser.create(params).id
        flushAndClear()

        then:
        SecUser user = SecUser.get(id)
        user.login == 'galt'
        SecRoleUser.findAllByUser(user)*.role.id == [1L, 2L]

    }

    def "test create with roles obj"() {
        when:
        // use objects that may get passed in from json in this format
        def params = getUserParams([
            login: 'galt2',
            email: 'test2@9ci.com',
            roles: [
                [id: 2 ], [id: 3]
            ]
        ])
        Long id2 = SecUser.create(params).id
        flushAndClear()

        then:
        SecUser user2 = SecUser.get(id2)
        user2.login == 'galt2'
        SecRoleUser.findAllByUser(user2)*.role.id == [2L, 3L]
    }


    def "test updating roles"() {
        when:
        //assert current admin has 1 role id:1
        assert SecRoleUser.getByUser(1)*.role.id == [1L]
        Map params = [
            id:1,
            roles: [2, 3]
        ]
        SecUser.update(params)
        flush()
        SecUser user = SecUser.get(1)

        then:
        SecRoleUser.findAllByUser(SecUser.get(1))*.role.id == [2L, 3L]

        when:
        Map params2 = [
            id:1,
            roles: [1]
        ]
        SecUser.update(params2)
        flush()

        then:
        SecRoleUser.findAllByUser(SecUser.get(1))*.role.id == [1L]
    }

    def "remove roles when user is removed"() {
        setup:
        Map params = getUserParams([roles: ["1", "2"]])
        SecUser user = SecUser.create(params)
        flushAndClear()

        expect:
        SecRoleUser.get(user.id, 1)
        SecRoleUser.get(user.id, 2)

        when:
        secUserRepo.remove(user)

        then:
        !SecRoleUser.get(user.id, 1)
        !SecRoleUser.get(user.id, 2)
    }

    def testRemove() {
        setup:
        SecUser user = secUserRepo.create(getUserParams())

        expect:
        SecUser.get(user.id) != null

        when:
        secUserRepo.remove(user)

        then:
        SecUser.get(user.id) == null
    }

    /** printDiffs prints the pertinent params and final data for the test for debugging purposes. */
    void printDiffs(Map params, SecUser user, Map result) {
        println "          key                    params - result"
        def format = '    %7s.%-10s: %15s - %-15s\n'
        printf(format, 'user', 'login', params.login, user.login)
        ['firstName', 'lastName', 'name', 'email'].each { key ->
            printf(format, 'contact', key, params.contact[key], user.contact[key])
        }
        println "result is ${result}"
        println "user id: ${user.id}, login: ${user.login}, contact id: ${user.contact.id}, firstName: ${user.contact.firstName}"
    }
}
