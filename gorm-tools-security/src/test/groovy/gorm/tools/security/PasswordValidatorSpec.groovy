package gorm.tools.security

import gorm.tools.security.domain.AppUser
import yakworks.problem.IProblem
import yakworks.gorm.testing.SecurityTest
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Specification

class PasswordValidatorSpec extends Specification implements  DataRepoTest, SecurityTest {

    void setup() {
        mockDomain(AppUser)
        new AppUser(name: "admin", username:"admin", email:"admin@9ci.com", password: "test").persist()
    }

    void test_validate() {
        setup:
        PasswordValidator validator = new PasswordValidator()

        when: "password length"
        validator.passwordMinLength = 4
        IProblem problem = validator.validate(AppUser.get(1), "123", "123")

        then:
        problem.violations.find{ it.code == "security.validation.password.minlength" }

        when: "password match"
        validator.passwordMinLength = 3
        problem = validator.validate(AppUser.get(1), "123", "1234")

        then:
        problem.violations.find{ it.code == "security.validation.password.match" }

        when: "require lowercase"
        validator.passwordMinLength = 4
        validator.passwordMustContainLowercaseLetter = true
        problem = validator.validate(AppUser.get(1), "ABCD", "ABCD")

        then:
        problem.violations.find{ it.code == "security.validation.password.mustcontain.lowercase" }

        when: "require uppercase"
        validator.passwordMinLength = 4
        validator.passwordMustContainUpperaseLetter = true
        problem = validator.validate(AppUser.get(1), "abcd", "abcd")

        then:
        problem.violations.find{ it.code == "security.validation.password.mustcontain.uppercase" }

        when: "require numbers"
        validator.passwordMinLength = 4
        validator.passwordMustContainNumbers = true
        problem = validator.validate(AppUser.get(1), "abcD", "abcD")

        then:
        problem.violations.find{ it.code == "security.validation.password.mustcontain.numbers" }


        when: "require symbol"
        validator.passwordMinLength = 4
        validator.passwordMustContainSymbols = true
        problem = validator.validate(AppUser.get(1), "ab1D", "ab1D")

        then:
        problem.violations.find{ it.code == "security.validation.password.mustcontain.symbol" }

        when: "all good"
        validator.passwordMinLength = 4
        validator.passwordMustContainSymbols = true
        def result = validator.validate(AppUser.get(1), "ab1D#", "ab1D#")

        then:
        result.ok == true

        cleanup:
        validator.passwordMinLength = 4
        validator.passwordMustContainLowercaseLetter = false
        validator.passwordMustContainUpperaseLetter = false
        validator.passwordMustContainSymbols = false
        validator.passwordMustContainNumbers = false
    }
}
