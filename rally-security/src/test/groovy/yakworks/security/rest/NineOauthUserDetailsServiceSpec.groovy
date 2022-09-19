package yakworks.security.rest

import org.springframework.beans.factory.annotation.Autowired
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.testing.gorm.unit.DataRepoTest
import grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.testing.services.ServiceUnitTest
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.pac4j.core.profile.CommonProfile
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UserDetailsService
import spock.lang.Specification

class NineOauthUserDetailsServiceSpec extends Specification implements DataRepoTest{
    static entityClasses = [AppUser, SecRole, SecRoleUser]

    @Autowired NineOauthUserDetailsService nineOauthUserDetailsService

    Closure doWithGormBeans(){{ ->
        preAuthenticationChecks(InstanceFactoryBean, Mock(UserDetailsChecker), UserDetailsChecker)
        userDetailsService(InstanceFactoryBean, Mock(UserDetailsService), UserDetailsService)
        passwordEncoder(NoOpPasswordEncoder)
        nineOauthUserDetailsService(NineOauthUserDetailsService)
    }}

    void "load user shoud not fail when no password"() {
        setup:
        AppUser appUser = build(AppUser, [username: "test", email: "test@9ci.com"])

        expect:
        AppUser.findByUsername("test")

        when:
        CommonProfile profile = Mock(CommonProfile) {
            getId() >> "test"
        }

        OauthUser oauthUser = nineOauthUserDetailsService.loadUser(profile, [])

        then:
        oauthUser != null
        oauthUser.password == "N/A"
        oauthUser.username == "test"
    }
}
