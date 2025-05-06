package yakworks.security.spring.token

import java.time.temporal.ChronoUnit

import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt

import spock.lang.Specification
import yakworks.security.spring.token.generator.JwtSymmetricTokenGenerator

class JwtSymmetricTokenGeneratorSpec extends Specification {

    void "HSA generate"() {
        setup:
        def auth = new TestingAuthenticationToken("bob", null, "ADMIN")
        def tokenGenerator = new JwtSymmetricTokenGenerator()
        tokenGenerator.jwtProperties = new JwtProperties(secret: "s/9Y3WUi5LkKsR8IZ4DTcXAAFDlkjL12")

        when:
        Jwt token = tokenGenerator.generate(auth)

        then:
        token.issuedAt
        token.expiresAt
        token.subject == 'bob'
        token.claims.jti == '1234'

        when: "decode it"
        sleep 1000
        def decoded = tokenGenerator.jwtDecoder.decode(token.tokenValue)

        then:
        decoded.subject == token.subject
        decoded.issuedAt == token.issuedAt.truncatedTo(ChronoUnit.SECONDS)
        decoded.expiresAt  == token.expiresAt.truncatedTo(ChronoUnit.SECONDS)
        decoded.claims.jti == token.claims.jti
        // token.tokenValue == "foo"

    }

}
