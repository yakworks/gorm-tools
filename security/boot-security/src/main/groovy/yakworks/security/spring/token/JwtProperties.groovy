/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties
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
        /** RS256 or ES256 supported and tested */
        SignatureAlgorithm alg = SignatureAlgorithm.RS256
        /** issuer in form "http://yakworks.org" */
        String iss
        /** the public RSA key */
        Resource publicKey
        /** the private RSA key */
        Resource privateKey

        /**
         * For EC ES256, has a single key with both private and public by default
         * Can also generate a single RSA pem and assign it here as well.
         */
        Resource pairKey

        /** cached keyPair */
        KeyPair keyPair

        /** gets the keypair using the configured resources for pairKey, publikKey and/or privateKey */
        KeyPair getKeyPair(){
            if(this.keyPair) return this.keyPair

            String rythym = isEC() ? "EC" : "RSA"
            //keyPair should really only work for EC/ES... keys. RSA will have 2, public and private specified
            if(pairKey) {
                this.keyPair = PemUtils.parseKeyPair(pairKey)
            } else {
                PrivateKey priv = this.privateKey?.exists() ? PemUtils.readPrivateKeyFromFile(this.privateKey, rythym) : null
                PublicKey pub = this.publicKey?.exists()  ? PemUtils.readPublicKeyFromFile(this.publicKey, rythym) : null
                this.keyPair =  new KeyPair(pub, priv)
            }
            return this.keyPair
        }

        /**
         * If alg (Algorithm) starts with "ES" then its an EC (Elliptic Curve) type
         */
        boolean isEC(){
            return alg?.name?.startsWith("ES")
        }
    }

}
