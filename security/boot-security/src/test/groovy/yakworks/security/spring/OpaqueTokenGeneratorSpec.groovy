package yakworks.security.spring

import java.time.Instant

import spock.lang.Specification
import yakworks.security.spring.token.JwtProperties
import yakworks.security.spring.token.OpaqueTokenGenerator

class OpaqueTokenGeneratorSpec extends Specification {

    void "OpaqueTokenGenerator generate"() {
        setup:
        def tokenGenerator = new OpaqueTokenGenerator()
        tokenGenerator.jwtProperties = new JwtProperties()

        when:
        def token = tokenGenerator.generate(null)

        then:
        token.tokenValue.size() == 32
        token.tokenValue.startsWith(tokenGenerator.jwtProperties.tokenPrefix)
        token.expiresAt >= Instant.now()
    }

}
