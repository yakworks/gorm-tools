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
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix="app.security.jwt")
@CompileStatic
class JwtProperties {

    //NOT USED WIP to config the type of JWT. HS256 or RS256
    String type

    //secret for Symmetric HS256 tokens, about 32+ chars of randomness
    String secret // = "s/9Y3WUi5LkKsR8IZ4DTcX="

    /** token expiration seconds, 10 min */
    long expiry = 600L // = 10min

    /** the prefix of the token if its an opaque one, not applicable to jwt */
    String tokenPrefix = 'opq_'

    /** TODO Audience should be the api url? */
    String audience

    Map<String, Issuer> issuers

    Issuer getDefaultIssuer(){
        // assert issuers['default']
        return issuers['default']
    }

    static class Issuer {
        String type
        String iss //issuer
        RSAPublicKey rsaPublicKey
        RSAPrivateKey rsaPrivateKey
        Resource ecPublicKey
        Resource ecPrivateKey

        PublicKey getPublicKey() {
            return rsaPublicKey //?: ecPublicKey
        }

        PrivateKey getPrivateKey() {
            return rsaPrivateKey //?: ecPrivateKey
        }

        KeyPair getKeyPair(){ return new KeyPair(publicKey, privateKey) }
    }

}
