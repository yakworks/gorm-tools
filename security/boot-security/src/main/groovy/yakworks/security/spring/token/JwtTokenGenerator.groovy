/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import java.time.Instant
import java.time.temporal.ChronoUnit

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.AbstractOAuth2Token
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder

import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext

/**
 * generates a JWT.
 */
@CompileStatic
class JwtTokenGenerator implements TokenGenerator<Jwt> {

    @Autowired JwtEncoder jwtEncoder
    @Autowired JwtProperties jwtProperties

    JwtEncoder getJwtEncoder(){
        if(!this.jwtEncoder) {
            JWK jwk = new RSAKey.Builder(jwtProperties.publicKey).privateKey(jwtProperties.privateKey).build();
            JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
            jwtEncoder = new NimbusJwtEncoder(jwks);
        }
        return jwtEncoder
    }

    @Override
    Jwt generate(Authentication authentication) {
        String scope = authentication.authorities.collect { it.authority }.join(' ')
        Instant now = Instant.now()
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(jwtProperties.expiry))
            .subject(authentication.name)
            .claim("scope", scope)
            .build()

        JwtEncoderParameters encodeParams = JwtEncoderParameters.from(claims)

        return jwtEncoder.encode(encodeParams)
    }

}
