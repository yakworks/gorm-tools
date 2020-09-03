package gorm.tools.security


import gorm.tools.security.domain.SecRoleUser
import gorm.tools.security.domain.SecUser
import gorm.tools.security.domain.SecUserRepo
import gorm.tools.security.testing.SecuritySpecHelper
import gorm.tools.testing.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class UserRepoSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {
    SecUserRepo secUserRepo

    Map getUserParams(params = [:]){
        return ([
                name:"test-user",
                email:"test@9ci.com",
                login:'lll',
                 password:'secretStuff',
                 repassword:'secretStuff',
                 userRole:'1',
                 '_inactive':null,
                 id:'1',
        ] << params)
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

    def testInsertWithRoles() {
        when:
        Map params = getUserParams([roles: ["1", "2"]])
        params.remove 'id'
        params.remove 'version'
        SecUser res = secUserRepo.create(params)
        flushAndClear()

        then:
        res.id != null

        when:
        SecUser user = SecUser.get(res.id)

        then:
        user.login == 'lll'
        SecRoleUser.findAllByUser(user)*.role.id == [1L, 2L]
    }

    def testUpdateToAddRoles() {
        when:
        Map params = getUserParams([roles: ["1", "2"]])
        SecUser res = secUserRepo.update([:], params)
        flushAndClear()

        then:
        res.id != null

        when:
        SecUser user = SecUser.get(1)

        then:
        user.login == 'lll'
        SecRoleUser.findAllByUser(user)*.role.id == [1L, 2L]
    }

    def testRemoveRoles() {
        setup:
        Map params = getUserParams([roles: ["1", "2"]])
        params.remove 'id'
        params.remove 'version'
        SecUser user = secUserRepo.create(params)
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

    def testUpdateToReplaceRoles() {
        when:
        SecRoleUser.findAllByUser(SecUser.get(1))*.role.id == [1L]
        Map params = getUserParams([roles: ["2", "3"]])
        SecUser res = secUserRepo.update([:], params)
        flushAndClear()

        then:
        res.id != null

        when:
        SecUser user = SecUser.get(1)

        then:
        user.login == 'lll'
        SecRoleUser.findAllByUser(user)*.role.id == [2L, 3L]
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
