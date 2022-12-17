package yakworks.security.spring.token

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.ECGenParameterSpec

import org.apache.commons.codec.binary.Base64
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import spock.lang.Specification

class PemUtilsSpec extends Specification {

    static String ES256_JWT="eyJhbGciOiJFUzI1NiJ9.SGVsbG8gd29ybGQh.n4M4kAuOk939Kgh1DLbJU2C18nf3txxpBF82QNEDHHOTlx6evlCTM_-I3fT78eVcebCTPnyNmsMQMKGVH3_gIQ"
    static String RS256_JWT="eyJhbGciOiJSUzI1NiJ9.SGVsbG8gd29ybGQh.JZI4UTljHIY7K6l1TPkr_42fnxI5lFFw-XtHRpXcvjXzGliEb6TBuBjQLf_lPQoy8GN1itF-ZhfUGPo06uDzz06psmXc955e_oFzPyJCOruPLFF1aemtECPkvw7azCFAAoh7oAnSTg2ObSV2lKJClRJR7RZgC31wjr45-_YIdw0uTusfqRBjsIpq1-vaHZSRd5wb-ZBht2Tdulw9MSo7RvpkVa3OSQ-SBE32defUoqBBbjY5oY0DhQgDTTmXz2fyowHXLSAAso13n35ZvgIBrz-VtdceJc_NB95DvuP759Pi-Cm88xrnTBqQx03YblEj-5JxbBgas0ZmpcuL9oyOHA"

    def "GenerateRsaKey"() {
        expect:
        TokenUtils.generateRsaKey()
    }

    def "ec256 keypair"() {
        when:
        KeyPair keyPair = PemUtils.parseKeyPair(new ClassPathResource("ec256-key-pair.pem"))

        // Get private + public EC key
        ECPrivateKey privateKey = (ECPrivateKey)keyPair.getPrivate();
        ECPublicKey publicKey = (ECPublicKey)keyPair.getPublic();

        // Sign test
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.ES256), new Payload("Hello world!"));
        jwsObject.sign(new ECDSASigner(privateKey));

        // Serialise
        String compactJWS = jwsObject.serialize();

        // Verify test
        jwsObject = JWSObject.parse(compactJWS);

        then:
        jwsObject.verify(new ECDSAVerifier(publicKey))
    }

    def "verify ec256"() {
        when:
        ECPublicKey publicKey = (ECPublicKey)PemUtils.readPublicKeyFromFile(new ClassPathResource("ec256-public.pem"), "EC")
        def jwsObject = JWSObject.parse(ES256_JWT)

        then:
        jwsObject.verify(new ECDSAVerifier(publicKey))

    }

    def "rs256 keypair"() {
        when:
        PrivateKey privateKey = PemUtils.readPrivateKeyFromFile(new ClassPathResource("rs256-private.pem"), "RSA")
        PublicKey publicKey = PemUtils.readPublicKeyFromFile(new ClassPathResource("rs256-public.pem"), "RSA")
        def keyPair = new KeyPair(publicKey, privateKey);

        // Sign test
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.RS256), new Payload("Hello world!"))
        jwsObject.sign(new RSASSASigner(privateKey))

        // Serialise
        String compactJWS = jwsObject.serialize()

        // Verify test
        jwsObject = JWSObject.parse(compactJWS)

        then:
        jwsObject.verify(new RSASSAVerifier(publicKey))
    }

    def "verify ec256"() {
        when:
        RSAPublicKey publicKey = (RSAPublicKey)PemUtils.readPublicKeyFromFile(new ClassPathResource("rs256-public.pem"), "RSA")
        def jwsObject = JWSObject.parse(RS256_JWT)

        then:
        jwsObject.verify(new RSASSAVerifier(publicKey))

    }

}
