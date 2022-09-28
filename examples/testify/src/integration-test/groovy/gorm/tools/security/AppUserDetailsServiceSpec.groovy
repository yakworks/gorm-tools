package gorm.tools.security

import yakworks.security.gorm.AppUserService

import java.time.LocalDateTime

import yakworks.security.gorm.model.AppUser
import yakworks.security.spring.AppUserDetailsService
import yakworks.security.spring.SpringUserInfo
import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification

@Integration
@Rollback
class AppUserDetailsServiceSpec extends Specification implements DataIntegrationTest {
    AppUserDetailsService userDetailsService
    AppUserService appUserService

    void testLoadUserByUsername() {
        when:
        AppUser.repo.create([username:"karen", password:"karen", repassword:"karen", email:"karen@9ci.com"])
        flush()
        SpringUserInfo gUser = userDetailsService.loadUserByUsername('karen')

        then:
        gUser != null

        when:
        AppUser user = AppUser.get(gUser.id)

        then:
        user.name== 'karen'
    }

    void "test when user has empty password when using token"() {
        when:
        AppUser.repo.create([username:"karen", repassword:"karen", email:"karen@9ci.com"])
        flush()
        SpringUserInfo gUser = userDetailsService.loadUserByUsername('karen')

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
        appUserService.passwordExpiryEnabled = true
        appUserService.passwordExpireDays = 10
        user.passwordExpired = true
        user.passwordChangedDate = LocalDateTime.now().minusDays(11)
        user.persist()

        when:
        SpringUserInfo nineUser = userDetailsService.loadUserByUsername(user.username, false)

        then:
        nineUser.credentialsNonExpired == false

        cleanup:
        appUserService.passwordExpiryEnabled = false
        appUserService.passwordExpireDays = 30
    }

}
