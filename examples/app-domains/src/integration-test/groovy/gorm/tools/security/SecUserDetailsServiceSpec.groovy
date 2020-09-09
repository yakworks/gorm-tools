package gorm.tools.security


import gorm.tools.security.domain.SecUser
import gorm.tools.testing.integration.DataIntegrationTest
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class SecUserDetailsServiceSpec extends Specification implements DataIntegrationTest {
    SecUserDetailsService userDetailsService

    void testLoadUserByUsername() {
        when:
        SecUser.repo.create([username:"karen", password:"karen", repassword:"karen", email:"karen@9ci.com"])
        flush()
        GrailsUser gUser = userDetailsService.loadUserByUsername('karen')

        then:
        gUser != null

        when:
        SecUser user = SecUser.get(gUser.id)

        then:
        user.name== 'karen'
    }

    void "test expired password"() {
        given:
        Date now = new Date()
        SecUser user = SecUser.first()
        userDetailsService.passwordExpireEnabled = true
        userDetailsService.passwordExpireDays = 10
        user.passwordExpired = true
        user.passwordChangedDate = now - 11
        user.save()

        when:
        GrailsUser nineUser = userDetailsService.loadUserByUsername(user.username, false)

        then:
        nineUser.credentialsNonExpired == false

        cleanup:
        userDetailsService.passwordExpireEnabled = false
        userDetailsService.passwordExpireDays = 30
    }

}
