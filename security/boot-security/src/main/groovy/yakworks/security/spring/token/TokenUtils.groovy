/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic

import org.springframework.security.oauth2.core.AbstractOAuth2Token

/**
 * helpers to generate key pairs, RSA right now.
 */
@CompileStatic
class TokenUtils {
    public static String COOKIE_NAME = "TOK"

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
     * Generates a keypair for RSA 2048
     */
    static KeyPair generateES256Key() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC")
            keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"))
            keyPair = keyPairGenerator.generateKeyPair()
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex)
        }
        return keyPair
    }

    /**
     * creates a cookie for the JWT token
     */
    static Cookie tokenCookie(HttpServletRequest request, AbstractOAuth2Token token) {
        Cookie tCookie = new Cookie( COOKIE_NAME, token.tokenValue )
        //FIXME some hard coded values to get it working
        tCookie.maxAge = getExpiresIn(token)
        tCookie.path = '/'
        //only works if its https, her for dev as its normal http most of time.
        if ( isHttps(request) ) {
            tCookie.setHttpOnly(true)
            tCookie.setSecure(true)
        }
        return tCookie
    }

    /**
     * Checks to see if base Uri starts with https. if its http then true
     */
    static boolean isHttps(HttpServletRequest request) {
        request.getRequestURL().toString().startsWith('https')
    }

    /**
     * gets the expires in int value from token
     */
    static int getExpiresIn(AbstractOAuth2Token token) {
        if (token.expiresAt != null) {
            return ChronoUnit.SECONDS.between(Instant.now(), token.expiresAt).toInteger()
        }
        return -1
    }

    /**
     * converts token to map so can easily be sent to json
     */
    static Map tokenToMap(AbstractOAuth2Token token) {
        Map body = [
            token_type: 'Bearer',
            access_token: token.tokenValue,
            "expires_in": getExpiresIn(token)
        ]
        return body
    }

}
