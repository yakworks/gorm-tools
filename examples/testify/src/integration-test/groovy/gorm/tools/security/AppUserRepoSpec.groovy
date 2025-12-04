package gorm.tools.security

import gorm.tools.problem.ValidationProblem
import org.springframework.security.crypto.password.PasswordEncoder
import yakworks.api.problem.data.DataProblemException
import yakworks.security.PasswordConfig
import yakworks.security.Roles
import yakworks.security.gorm.model.SecPasswordHistoryRepo
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.AppUserRepo
import yakworks.testing.gorm.integration.SecuritySpecHelper
import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification

import javax.inject.Inject

@Integration
@Rollback
class AppUserRepoSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    AppUserRepo appUserRepo
    SecPasswordHistoryRepo secPasswordHistoryRepo
    PasswordEncoder passwordEncoder
    @Inject PasswordConfig passwordConfig

    Map getUserParams(Map params = [:]){
        Map baseParams = [
            name:"John Galt",
            email:"test@9ci.com",
            username:'galt',
            password:'secretStuff',
            repassword:'secretStuff',
        ]
        //baseParams.putAll params
        return ([:] << baseParams << params)
    }

    void "test create"() {
        when:
        Map params = getUserParams()
        Long id = AppUser.create(params).id
        flushAndClear()

        then:
        AppUser user = AppUser.get(id)
        user.username == 'galt'
        user.email == params.email
        user.name == params.name
        passwordEncoder.matches(params.password, user.passwordHash)
    }

    void "test unique username"() {
        setup: "this creates initial user"
        Map params = getUserParams()
        AppUser.create(params)
        flushAndClear()

        when: "username exists"
        AppUser.create(params)

        then: "Fails"
        Exception ex = thrown()
        ex instanceof DataProblemException
        ex.message.contains "Violates unique constraint [username: galt]"

        when: "success"
        params.username = "galt2"
        AppUser.create(params)

        then:
        noExceptionThrown()
    }

    void "test create with roles ids"() {
        when:
        // should convert the strings to long
        Map params = getUserParams([roles: [Roles.ADMIN, "MANAGER"]])
        Long id = AppUser.create(params).id
        flushAndClear()

        then:
        AppUser user = AppUser.get(id)
        user.username == 'galt'
        SecRoleUser.findAllByUser(user)*.role.id == [1L, 3L]

        appUserRepo.getRoles(user).size() == 2

        when: "verify permissions"
        Set perms = appUserRepo.getPermissions(user)

        then:
        perms.size()
        perms.size() == SecRole.query("code":['$in':[Roles.ADMIN, "MANAGER"]]).property("permissions").get().flatten().unique().size()
    }

    /*see SecuritySeedData*/
    void "test create with roles obj"() {
        when:
        // use objects that may get passed in from json in this format
        def params = getUserParams([
            username: 'galt2',
            email: 'test2@9ci.com',
            roles: [
                [id: 1 ], [id: 3]
            ]
        ])
        Long id2 = AppUser.create(params).id
        flushAndClear()

        then:
        AppUser user2 = AppUser.get(id2)
        user2.username == 'galt2'
        SecRoleUser.findAllByUser(user2)*.role.id == [1L, 3L]
    }


    void "test updating roles"() {

        when: "current admin has 2 roles 1,3"
        assert SecRoleUser.getByUser(1)*.role.id == [1, 3]

        Map updateParams = [
            id:1,
            roles: [[id: 2], [id: 3]]
        ]
        //update it to 2 and 3.
        AppUser.repo.update(updateParams)
        flushAndClear()

        AppUser user = AppUser.get(1)

        def roleIds = SecRoleUser.findAllByUser(AppUser.get(1))*.role.id

        then:
        roleIds.size() == 2
        roleIds.containsAll([3L, 2L])

        when:
        flushAndClear()
        Map params2 = [
            id: 1,
            roles: ["ADMIN"]
        ]
        AppUser.repo.update(params2)
        flush()

        then:
        SecRoleUser.findAllByUser(AppUser.get(1))*.role.id == [1L]
    }

    void "remove roles when user is removed"() {
        setup:
        Map params = getUserParams([roles: ["ADMIN", "MANAGER"]])
        AppUser user = AppUser.create(params)
        flushAndClear()

        expect:
        SecRoleUser.get(1, user.id)
        SecRoleUser.get("MANAGER", user.id)

        when:
        appUserRepo.remove(user)

        then:
        !SecRoleUser.get(1, user.id)
        !SecRoleUser.get(3, user.id)
    }

    void testRemove() {
        setup:
        AppUser user = appUserRepo.create(getUserParams())

        expect:
        AppUser.get(user.id) != null

        when:
        appUserRepo.remove(user)

        then:
        AppUser.get(user.id) == null
    }

    void "test updatePassword"() {
        setup:
        passwordConfig.historyEnabled = true
        passwordConfig.mustContainUppercaseLetter = true
        AppUser user = AppUser.get(1L)

        expect:
        user

        when: "same password, nothing changed"
        appUserRepo.updatePassword(user, '123')

        then: "no history created"
        noExceptionThrown()
        !secPasswordHistoryRepo.findAllByUser(user.id)

        when: "pwd validation fails"
        appUserRepo.updatePassword(user, 'test')

        then: "no history created"
        ValidationProblem.Exception ex = thrown()
        ex.violations.size() == 1
        ex.violations[0].code == 'security.validation.password.mustcontain.uppercase'
        //happens in ViolatinConverter
        //ex.violations[0].message == "Password must contain a uppercase lettere"

        and: "history not created"
        !secPasswordHistoryRepo.findAllByUser(user.id)

        when: "all good"
        appUserRepo.updatePassword(user, 'Xtest')
        flush()

        then:
        noExceptionThrown()

        and: "history created"
        secPasswordHistoryRepo.findAllByUser(user.id).size() == 1
    }

    /** printDiffs prints the pertinent params and final data for the test for debugging purposes. */
    void printDiffs(Map params, AppUser user, Map result) {
        println "          key                    params - result"
        def format = '    %7s.%-10s: %15s - %-15s\n'
        printf(format, 'user', 'username', params.login, user.username)
        ['firstName', 'lastName', 'name', 'email'].each { key ->
            printf(format, 'contact', key, params.contact[key], user.contact[key])
        }
        println "result is ${result}"
        println "user id: ${user.id}, username: ${user.username}, contact id: ${user.contact.id}, firstName: ${user.contact.firstName}"
    }
}
