package yakworks.security.spring.token

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.Security
import java.security.interfaces.ECKey
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.time.Instant
import java.security.interfaces.ECKey

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import spock.lang.Specification

import org.apache.commons.codec.binary.Base64

class TokenUtilsSpec extends Specification {

    static String ES256_JWT="eyJhbGciOiJFUzI1NiJ9.SGVsbG8gd29ybGQh.n4M4kAuOk939Kgh1DLbJU2C18nf3txxpBF82QNEDHHOTlx6evlCTM_-I3fT78eVcebCTPnyNmsMQMKGVH3_gIQ"

    def "GenerateRsaKey"() {
        expect:
        TokenUtils.generateRsaKey()
    }

    def "TokenCookie"() {
        when:
        def jwt = Jwt.withTokenValue("token").header("alg", "none").claim(JwtClaimNames.SUB, "user")
            .claim("scope", "read").build()

        then:
        TokenUtils.tokenCookie(new MockHttpServletRequest(), jwt)
    }

    // https://notes.salrahman.com/generate-es256-es384-es512-private-keys/
    // https://gist.github.com/ygotthilf/baa58da5c3dd1f69fae9
    def "es256 play"(){
        when:
        // Given
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1")); // == P256

        // When
        final KeyPair keyPair = keyPairGenerator.generateKeyPair();

        then:
        Base64.encodeBase64String(keyPair.getPublic().getEncoded())
        // log.info("ecKey.privateKey: {}", Base64.encodeBase64String(keyPair.getPrivate().getEncoded()));
    }

}
