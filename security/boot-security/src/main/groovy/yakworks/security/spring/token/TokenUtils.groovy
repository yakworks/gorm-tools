/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic

import org.springframework.security.oauth2.core.AbstractOAuth2Token
import org.springframework.security.oauth2.jwt.Jwt

/**
 * helpers to generate key pairs, RSA right now.
 */
@CompileStatic
class TokenUtils {
    public static String COOKIE_NAME = "jwt"

    /**
     * Generates a keypair for RSA 2048
     */
    static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    /**
     * creates a cookie for the JWT token
     */
    static Cookie jwtCookie(HttpServletRequest request, AbstractOAuth2Token token) {
        Cookie jwtCookie = new Cookie( 'jwt', token.tokenValue )
        //FIXME some hard coded values to get it working
        jwtCookie.maxAge = JwtTokenGenerator.getExpiresIn(token)
        jwtCookie.path = '/'
        //only works if its https, her for dev as its normal http most of time.
        if ( isHttps(request) ) {
            jwtCookie.setHttpOnly(true)
            jwtCookie.setSecure(true)
        }
        return jwtCookie
    }

    /**
     * Checks to see if base Uri starts with https. if its http then true
     */
    static boolean isHttps(HttpServletRequest request) {
        request.getRequestURL().toString().startsWith('https')
    }
}
