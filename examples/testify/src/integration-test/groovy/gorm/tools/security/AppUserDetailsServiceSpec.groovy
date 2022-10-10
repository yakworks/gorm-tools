package gorm.tools.security

import yakworks.security.gorm.AppUserService

import java.time.LocalDateTime

import yakworks.security.gorm.PasswordValidator
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.AppUserDetailsService
import yakworks.security.spring.user.SpringUser
import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification

@Integration
@Rollback
class AppUserDetailsServiceSpec extends Specification implements DataIntegrationTest {
    AppUserDetailsService userDetailsService
    AppUserService appUserService
    PasswordValidator passwordValidator

    void testLoadUserByUsername() {
        when:
        AppUser.repo.create([username:"karen", password:"karen", repassword:"karen", email:"karen@9ci.com"])
        flush()
        SpringUser gUser = userDetailsService.loadUserByUsername('karen')

        then:
        gUser != null
        gUser.getDisplayName()

        when:
        AppUser user = AppUser.get(gUser.id)

        then:
        user.name== 'karen'
    }

    void "test when user has empty password when using token"() {
        when:
        AppUser.repo.create([username:"karen", repassword:"karen", email:"karen@9ci.com"])
        flush()
        SpringUser gUser = userDetailsService.loadUserByUsername('karen')

        then:
        gUser != null

        when:
        AppUser user = AppUser.get(gUser.id)

        then:
        user.name== 'karen'
    }

    //FIXME add a test for when credentialsNonExpired = true
    void "test expired password"() {
        given:
        AppUser user = AppUser.first()
        passwordValidator.passwordExpiryEnabled = true
        passwordValidator.passwordExpireDays = 10
        user.passwordExpired = true
        user.passwordChangedDate = LocalDateTime.now().minusDays(11)
        user.persist()

        when:
        SpringUser nineUser = userDetailsService.loadUserByUsername(user.username)

        then:
        nineUser.credentialsNonExpired == false

        cleanup:
        passwordValidator.passwordExpiryEnabled = false
        passwordValidator.passwordExpireDays = 30
    }

}
