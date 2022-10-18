/*
* Copyright 2020-2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

/**
 * A controller for the token resource.
 *
 * @author Josh Cummings
 */
@RestController
@CompileStatic
class TokenController {

    @Autowired JwtTokenGenerator tokenGenerator

    // @Value('${grails.serverURL:""}')
    // String serverURL

    // for dev and testing to make it easier to dump token into variable.
    // ex: `$ TOKEN=`http POST admin:123@localhost:8080/token.txt -b`
    @PostMapping("/token.txt")
    String tokenTxt() {
        return tokenGenerator.genererate().tokenValue
    }

    @PostMapping("/token")
    ResponseEntity<Map> token(HttpServletRequest request, HttpServletResponse response) {
        Jwt token = tokenGenerator.genererate()
        //add it as a cookie
        Cookie cookie = jwtCookie(request, token)
        response.addCookie(cookie)
        //convert to a Map to render it as json
        Map body = JwtTokenGenerator.tokenToMap(token)

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(body)
    }

    protected Cookie jwtCookie(HttpServletRequest request, Jwt token) {
        Cookie jwtCookie = new Cookie( 'jwt', token.tokenValue )
        //FIXME some hard coded values to get it working
        jwtCookie.maxAge = JwtTokenGenerator.getExpiresIn(token)
        jwtCookie.path = '/'
        //only works if its https, her for dev as its normal http most of time.
        if ( isHttps(request) ) {
            jwtCookie.setHttpOnly(true)
            jwtCookie.setSecure(true)
        }
        jwtCookie
    }

    /**
     * Checks to see if base Uri starts with https. if its http then true
     */
    protected boolean isHttps(HttpServletRequest request) {
        // String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
        //     .replacePath(null)
        //     .build()
        //     .toUriString();
        request.getRequestURL().toString().startsWith('https')
    }



}
