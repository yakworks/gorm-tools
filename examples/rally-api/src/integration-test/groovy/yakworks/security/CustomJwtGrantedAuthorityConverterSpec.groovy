package yakworks.security

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.jwt.Jwt
import spock.lang.Specification
import yakworks.security.spring.token.CustomJwtGrantedAuthorityConverter
import yakworks.security.spring.token.generator.JwtTokenExchanger
import yakworks.testing.gorm.integration.DataIntegrationTest

import javax.inject.Inject

@Integration
@Rollback
class CustomJwtGrantedAuthorityConverterSpec extends Specification implements DataIntegrationTest {

    @Inject JwtTokenExchanger jwtTokenExchanger
    @Inject UserDetailsService userDetailsService

    void "test convert"() {
        setup:
        CustomJwtGrantedAuthorityConverter converter = new CustomJwtGrantedAuthorityConverter(userDetailsService)
        Jwt jwt = jwtTokenExchanger.exchange("admin")

        when:
        Collection<GrantedAuthority> authorities = converter.convert(jwt)

        then:
        authorities
        authorities.size() == userDetailsService.loadUserByUsername("admin").authorities.size()
    }
}
