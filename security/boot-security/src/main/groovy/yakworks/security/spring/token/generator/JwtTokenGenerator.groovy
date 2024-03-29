/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token.generator


import java.time.Instant

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters

import yakworks.security.spring.token.JwtProperties

/**
 * generates a JWT.
 */
@CompileStatic
class JwtTokenGenerator implements TokenGenerator<Jwt> {

    @Autowired JwtEncoder jwtEncoder
    @Autowired JwtProperties jwtProperties

    JwtEncoder getJwtEncoder(){
        // if(!this.jwtEncoder) {
        //     JwtProperties.Issuer issuer = jwtProperties.getDefaultIssuer()
        //     JWK jwk = new RSAKey.Builder(issuer.publicKey as RSAKey).privateKey(issuer.privateKey).build()
        //     JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk))
        //     jwtEncoder = new NimbusJwtEncoder(jwks)
        // }
        return jwtEncoder
    }

    @Override
    Jwt generate(Authentication authentication) {
        JwtProperties.Issuer issuer = jwtProperties.getDefaultIssuer()
        String scope = authentication.authorities.collect { it.authority }.join(' ')
        Instant now = Instant.now()
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer.iss)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(jwtProperties.expiry))
            .subject(authentication.name)
            .claim("scope", scope)
            .build()

        SignatureAlgorithm alg = issuer.isEC() ? SignatureAlgorithm.ES256 : SignatureAlgorithm.RS256
        JwtEncoderParameters encodeParams = JwtEncoderParameters.from(
            JwsHeader.with(alg).build(),
            claims
        )

        return jwtEncoder.encode(encodeParams)
    }

    /*
    /Generating a safe HS256 Secret key
SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
String secretString = Encoders.BASE64.encode(key.getEncoded());
logger.info("Secret key: " + secretString);


     */
}
