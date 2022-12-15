/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token.generator

import java.security.SecureRandom

import groovy.transform.CompileStatic

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AccessToken

import yakworks.security.spring.token.store.TokenStore

/**
 * generates an Opaque (random) token and stores it for lookup.
 */
@CompileStatic
class StoreTokenGenerator implements TokenGenerator<OAuth2AccessToken> {

    //even though this is not jwt we use jwtProperties for the expiry
    @Autowired OpaqueTokenGenerator opaqueTokenGenerator
    @Autowired TokenStore tokenStore

    @Override
    OAuth2AccessToken generate(Authentication authentication) {
        OAuth2AccessToken otok = opaqueTokenGenerator.generate(authentication)
        tokenStore.storeToken(authentication.name, otok)
        return otok
    }

    String generateRandom(int count){
        RandomStringUtils.random(count, 0, 0, true, true, null, new SecureRandom())
    }

}
