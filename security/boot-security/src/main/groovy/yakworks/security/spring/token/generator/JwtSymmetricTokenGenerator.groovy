/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token.generator


import java.time.Instant
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import yakworks.security.spring.token.JwtProperties

/**
 * JWT with HS256 symmetric signing. meaning it uses a secret and not a key/pair like RS256 default
 * It makes a JWT that is much smaller, in the 140+ chars long, vs 450+ chars
 */
@CompileStatic
class JwtSymmetricTokenGenerator implements TokenGenerator<Jwt> {

    //even though this is not jwt we use jwtProperties for the expiry
    @Autowired JwtProperties jwtProperties

    @Override
    Jwt generate(Authentication authentication) {
        String scope = authentication.authorities.collect { it.authority }.join(' ')
        Instant now = Instant.now()

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now) //provides a sanity check.
            .expiresAt(now.plusSeconds(jwtProperties.expiry)) //keep in token so that we can check this and skip lookup if its expired.
            .subject(authentication.name) //put user id here?
            .id('1234') //the jti unique id for lookup in db.
            .build()

        JwsAlgorithm jwsAlgorithm = () -> "HS256"
        JwsHeader jwsHeader = JwsHeader.with(jwsAlgorithm).build()
        JwtEncoderParameters encodeParams = JwtEncoderParameters.from(jwsHeader, claims)
        def encoder = getJwtEncoder()

        return encoder.encode(encodeParams)
    }

    JwtEncoder getJwtEncoder() {
        JWKSource<SecurityContext> immutableSecret = new ImmutableSecret<SecurityContext>(secretKey)
        return new NimbusJwtEncoder(immutableSecret)
    }

    JwtDecoder getJwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
        return jwtDecoder
    }

    byte[] getSecret() {
        jwtProperties.secret.getBytes()
    }

    SecretKey getSecretKey() {
        new SecretKeySpec(secret, "HmacSHA256")
    }
}
