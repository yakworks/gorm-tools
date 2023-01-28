package yakworks.security

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.jwt.Jwt

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.security.spring.token.generator.JwtTokenExchanger
import yakworks.testing.gorm.integration.DataIntegrationTest

@Integration
@Rollback
class JwtTokenExchangerTest extends Specification implements DataIntegrationTest {

    JwtTokenExchanger jwtTokenExchanger

    def "test exchange with name"(){
        when:
        Jwt jwt = jwtTokenExchanger.exchange("admin")

        then:
        jwt.subject == 'admin'
    }

    def "test exchange with name not found"(){
        when:
        jwtTokenExchanger.exchange("some_bad_name")

        then:
        thrown(UsernameNotFoundException)
    }
}
