package yakworks.security.gorm

import java.time.LocalDate
import javax.inject.Inject

import org.apache.commons.lang3.RandomStringUtils

import gorm.tools.problem.ValidationProblem
import spock.lang.Specification
import yakworks.api.problem.Problem
import yakworks.security.PasswordConfig
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecPasswordHistory
import yakworks.security.services.PasswordValidator
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

import java.time.LocalDateTime

class PasswordSpec extends Specification implements  GormHibernateTest, SecurityTest {
    static entityClasses = [AppUser, SecPasswordHistory]

    @Inject PasswordConfig passwordConfig
    @Inject AppUserPasswordValidator passwordValidator

    void setup() {
        new AppUser(name: "admin", username:"admin", email:"admin@9ci.com", password: "test").persist()
    }

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

    void "sanity check"() {
        expect:
        passwordConfig
        passwordValidator
    }

    void "update password"() {
        setup:
        Map data = buildMap([password:"test"])

        when: "create"
        AppUser user = AppUser.create(data)
        user.persist(flush: true)

        then:
        user.passwordHash

        when: "update"
        String oldHash = user.passwordHash
        user = AppUser.update(id:user.id, newPassword:"newp", repassword:"newp")
        flush()

        then:
        oldHash != user.passwordHash
        user.passwordChangedDate.toLocalDate() == LocalDate.now()

        and:
        SecPasswordHistory.query(userId:user.id).count() == 0
    }

    void "test password history is created"() {
        setup:
        passwordConfig.historyEnabled = true
        Map data = buildMap([password:"test"])

        when: "create"
        AppUser user = AppUser.create(data)
        user.persist(flush: true)

        then:
        SecPasswordHistory.query(userId:user.id).count() == 1

        when:
        AppUser.update(id:user.id, newPassword:"newp2", repassword:"newp2")
        flush()

        then:
        SecPasswordHistory.query(userId:user.id).count() == 2

        cleanup:
        passwordConfig.historyEnabled = false
    }

    void "password exists in history"() {
        setup:
        passwordConfig.historyEnabled = true
        Map data = buildMap([password:"test"])

        when: "create"
        AppUser user = AppUser.create(data)

        then:
        noExceptionThrown()

        when: "not in history"
        AppUser.update(id:user.id, newPassword:"newp3", repassword:"newp3")
        flush()

        then:
        noExceptionThrown()

        when: "in history"
        AppUser.update(id:user.id, newPassword:"test", repassword:"test")
        flush()

        then:
        ValidationProblem.Exception ex = thrown()
        ex.violations.size() == 1
        ex.violations.find { it.code == 'security.validation.password.existsinhistory'}

        cleanup:
        passwordConfig.historyEnabled = true
    }

    void test_validate() {
        setup:
        PasswordValidator validator = new PasswordValidator(passwordConfig: passwordConfig)

        when: "password length"
        passwordConfig.minLength = 4
        Problem problem = validator.validate( "123")

        then:
        problem.violations.find{ it.code == "security.validation.password.minlength" }

        when: "require lowercase"
        passwordConfig.minLength = 4
        passwordConfig.mustContainLowercaseLetter = true
        problem = validator.validate("ABCD")

        then:
        problem.violations.find{ it.code == "security.validation.password.mustcontain.lowercase" }

        when: "require uppercase"
        passwordConfig.minLength = 4
        passwordConfig.mustContainUppercaseLetter = true
        problem = validator.validate("abcd")

        then:
        problem.violations.find{ it.code == "security.validation.password.mustcontain.uppercase" }

        when: "require numbers"
        passwordConfig.minLength = 4
        passwordConfig.mustContainNumbers = true
        problem = validator.validate("abcD")

        then:
        problem.violations.find{ it.code == "security.validation.password.mustcontain.numbers" }


        when: "require symbol"
        passwordConfig.minLength = 4
        passwordConfig.mustContainSymbols = true
        problem = validator.validate("ab1D")

        then:
        problem.violations.find{ it.code == "security.validation.password.mustcontain.symbol" }

        when: "all good"
        passwordConfig.minLength = 4
        passwordConfig.mustContainSymbols = true
        def result = validator.validate("ab1D#")

        then:
        result.ok == true

        cleanup:
        passwordConfig.minLength = 4
        passwordConfig.mustContainLowercaseLetter = false
        passwordConfig.mustContainUppercaseLetter = false
        passwordConfig.mustContainSymbols = false
        passwordConfig.mustContainNumbers = false
    }

    void "test isPasswordExpired"() {
        setup:
        passwordConfig.expiryEnabled = true
        passwordConfig.passwordExpireDays = 10

        Map data = buildMap([password:"test"])

        when: "create"
        AppUser user = AppUser.create(data)

        then:
        user
        user.passwordChangedDate
        user.passwordChangedDate.toLocalDate() == LocalDate.now()

        and:
        !passwordValidator.isPasswordExpired(user.id)

        when: "its expiry date"
        //set pwd to null, so it doesnt try to update it again, we are interested in expiry only
        user.password = null
        user.passwordChangedDate = LocalDateTime.now().minusDays(10)
        user.persist()

        then:
        !passwordValidator.isPasswordExpired(user.id)

        when: "its expired"
        user.passwordChangedDate = LocalDateTime.now().minusDays(11)
        user.persist(flush:true)

        then:
        passwordValidator.isPasswordExpired(user.id)

        when: "no password, does not expire"
        user.passwordHash = null
        user.passwordChangedDate = LocalDateTime.now().minusDays(11)
        user.persist()

        then:
        !passwordValidator.isPasswordExpired(user.id)
    }
}
