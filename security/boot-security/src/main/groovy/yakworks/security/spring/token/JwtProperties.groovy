/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix="app.security.jwt")
@CompileStatic
class JwtProperties {

    //WIP to config the type of JWT. HS256 or RS256
    String type

    //secret for Symmetric HS256 tokens
    String secret = "s/4KMb61LOrMYYAn4rfaQYSgr+le5SMrsMzKw8G6bXc="

    // keypair for RS256
    RSAPublicKey publicKey
    RSAPrivateKey privateKey

    /** token expiration seconds */
    long expiry = 600L

    /** Issuer key */
    String issuer = "self"

    /** the prefix of the token if its an opaque one, not applicable to jwt */
    String tokenPrefix = 'yak_'
}
