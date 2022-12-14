/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import java.security.SecureRandom
import java.time.Instant

import groovy.transform.CompileStatic

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AccessToken

/**
 * generates a JWT.
 */
@CompileStatic
class OpaqueTokenGenerator implements TokenGenerator<OAuth2AccessToken> {

    //even though this is not jwt we use jwtProperties for the expiry
    @Autowired JwtProperties jwtProperties

    SecureRandom random = new SecureRandom()

    @Override
    OAuth2AccessToken generate(Authentication authentication) {
        String prefix = jwtProperties.tokenPrefix
        assert prefix.size() == 4
        String token = "${prefix}${RandomStringUtils.randomAlphanumeric(28)}"

        Instant now = Instant.now()
        def oat = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            token,
            Instant.now(),
            now.plusSeconds(jwtProperties.expiry)
        )
        return oat
    }

}
