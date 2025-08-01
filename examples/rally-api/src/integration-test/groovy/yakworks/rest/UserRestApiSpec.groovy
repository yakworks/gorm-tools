package yakworks.rest

import org.springframework.http.HttpStatus

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import yakworks.rest.client.OkHttpRestTrait
import yakworks.security.PasswordConfig
import yakworks.security.gorm.model.AppUser

import javax.inject.Inject
import javax.transaction.Transactional

@Integration
class UserRestApiSpec extends Specification implements OkHttpRestTrait {

    String endpoint = "/api/rally/user"

    @Inject PasswordConfig passwordConfig

    def setup(){
        login()
    }

    void "user can be created without password"() {
        when:
        def resp = post(endpoint, [username:"test", email:"test@9ci.com"])

        then:
        resp.code() == HttpStatus.CREATED.value()

        when:
        def body = bodyToMap(resp)

        then:
        body
        body.id
        body.username == "test"

        when:
        AppUser user = AppUser.repo.getWithTrx(body.id as Long)

        then:
        !user.passwordHash
        !user.passwordChangedDate

        cleanup:
        if(user) {
            AppUser.withNewTransaction {
                AppUser.repo.removeById(user.id)
            }
        }
    }

    void "create with password"() {
        when:
        def resp = post(endpoint, [username:"test", password:'test', email:"test@9ci.com"])

        then:
        resp.code() == HttpStatus.CREATED.value()

        when:
        def body = bodyToMap(resp)

        then:
        body
        body.id
        body.username == "test"

        when:
        AppUser user = AppUser.repo.getWithTrx(body.id as Long)

        then:
        user.passwordHash
        user.passwordChangedDate

        cleanup:
        if(user) {
            AppUser.withNewTransaction {
                AppUser.repo.removeById(user.id)
            }
        }
    }

    void "test get to make sure display false dont get returned"() {
        when:
        def resp = get("$endpoint/1")
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.OK.value()
        body.id
        //shoudl not have the display:false fields
        !body.containsKey('passwordHash')
        !body.containsKey('resetPasswordToken')
    }

    void "password validation"() {
        setup:
        passwordConfig.minLength = 4
        passwordConfig.mustContainUppercaseLetter = true

        when:
        def resp = put("$endpoint/1", [newPassword:"12x", repassword:"12x"])
        Map body = bodyToMap(resp)

        then:
        body
        !body.ok
        body.title == 'AppUser Validation Error(s)'
        body.errors.size() == 2
        body.errors[0].code == 'security.validation.password.minlength'
        body.errors[0].message == 'password must be minimum 4 characters long'
        body.errors[1].code == 'security.validation.password.mustcontain.uppercase'
        body.errors[1].message == 'Password must contain uppercase letters'

        cleanup:
        passwordConfig.minLength = 3
        passwordConfig.mustContainUppercaseLetter = false
    }

    void "password in history"() {
        setup:
        passwordConfig.historyEnabled = true
        updatePassword(2L, "test")
        updatePassword(2L, "test2")

        when:
        def resp = put("$endpoint/2", [newPassword:"test", repassword:"test"])
        Map body = bodyToMap(resp)

        then:
        body
        !body.ok
        body.title == 'AppUser Validation Error(s)'
        body.errors.size() == 1
        body.errors[0].code == 'security.validation.password.existsinhistory'

        cleanup:
        passwordConfig.historyEnabled = false
        updatePassword(2L, "123")
    }

    @Transactional
    void updatePassword(Long uid, String pwd) {
         AppUser.update(id:uid, newPassword:pwd, repassword:pwd)
    }

}
