package gorm.tools.security

import java.time.LocalDateTime

import gorm.tools.security.domain.AppUser
import gorm.tools.testing.integration.DataIntegrationTest
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
class AppUserDetailsServiceSpec extends Specification implements DataIntegrationTest {
    AppUserDetailsService userDetailsService

    void testLoadUserByUsername() {
        when:
        AppUser.repo.create([username:"karen", password:"karen", repassword:"karen", email:"karen@9ci.com"])
        flush()
        GrailsUser gUser = userDetailsService.loadUserByUsername('karen')

        then:
        gUser != null

        when:
        AppUser user = AppUser.get(gUser.id)

        then:
        user.name== 'karen'
    }

    @Ignore //FIXME config is in userService now so fix test, also add a test for when credentialsNonExpired = true
    void "test expired password"() {
        given:
        AppUser user = AppUser.first()
        userDetailsService.passwordExpireEnabled = true
        userDetailsService.passwordExpireDays = 10
        user.passwordExpired = true
        user.passwordChangedDate = LocalDateTime.now().minusDays(11)
        user.persist()

        when:
        GrailsUser nineUser = userDetailsService.loadUserByUsername(user.username, false)

        then:
        nineUser.credentialsNonExpired == false

        cleanup:
        userDetailsService.passwordExpireEnabled = false
        userDetailsService.passwordExpireDays = 30
    }

}
