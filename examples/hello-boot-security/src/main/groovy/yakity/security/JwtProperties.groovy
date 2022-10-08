package yakity.security

import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix="app.security.jwt")
class JwtProperties {

    RSAPublicKey publicKey
    RSAPrivateKey privateKey

    long expiry = 60L
    String issuer = "self"

}
