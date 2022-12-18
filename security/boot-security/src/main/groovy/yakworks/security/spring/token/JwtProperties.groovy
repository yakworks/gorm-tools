/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.stereotype.Component

/**
 * Config from application.yml properteis.
 */
@Component
@ConfigurationProperties(prefix="app.security.jwt")
@CompileStatic
class JwtProperties {

    //secret for Symmetric HS256 tokens, about 32+ chars of randomness
    String secret // = "s/9Y3WUi5LkKsR8IZ4DTcX="

    /** default token expiration seconds, 10 min */
    long expiry = 600L // = 10min

    /** the prefix of the token if its an opaque one, not applicable to jwt */
    String tokenPrefix = 'opq_'

    /** TODO Audience should be the api url? */
    String audience

    Map<String, Issuer> issuers

    /** The issuer with the 'default' key */
    Issuer getDefaultIssuer(){
        // assert issuers['default']
        return issuers['default']
    }

    static class Issuer {
        SignatureAlgorithm alg = SignatureAlgorithm.RS256
        String iss //issuer
        RSAPublicKey rsaPublicKey
        RSAPrivateKey rsaPrivateKey
        Resource ecKeyPair
        Resource ecPublicKey
        // Resource ecPrivateKey

        PublicKey getPublicKey() {
            return getKeyPair().public
        }

        PrivateKey getPrivateKey() {
            return getKeyPair().private
        }

        KeyPair getKeyPair(){
            KeyPair keyPair
            if(isEC()) {
                if(this.ecKeyPair) {
                    keyPair = PemUtils.parseKeyPair(this.ecKeyPair)
                } else {
                    //needs at min a public key
                    keyPair =  new KeyPair(
                        PemUtils.readPublicKeyFromFile(ecPublicKey, "EC"),
                        null
                    )
                }
            } else {
                keyPair =  new KeyPair(rsaPublicKey, rsaPrivateKey)
            }
            return keyPair
        }

        /**
         * If alg (Algorithm) starts with "ES" then its an EC (Elliptic Curve) type
         */
        boolean isEC(){
            return alg?.name?.startsWith("ES")
        }
    }

}
