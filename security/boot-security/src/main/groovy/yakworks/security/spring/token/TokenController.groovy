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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import yakworks.security.spring.token.generator.JwtTokenGenerator
import yakworks.security.user.CurrentUser

/**
 * A controller for the token resource.
 */
@RestController
@CompileStatic
class TokenController {

    @Autowired JwtTokenGenerator jwtTokenGenerator
    // @Autowired OpaqueTokenGenerator opaqueTokenGenerator
    @Autowired CurrentUser currentUser

    // @Value('${grails.serverURL:""}')
    // String serverURL

    // for dev and testing to make it easier to dump token into variable.
    // ex: `$ TOKEN=`http POST admin:123@localhost:8080/token.txt -b`
    @PostMapping("/api/token.txt")
    String tokenTxt() {
        return jwtTokenGenerator.generate().tokenValue
    }

    @PostMapping("/api/token")
    ResponseEntity<Map> token(HttpServletRequest request, HttpServletResponse response) {
        Jwt token = jwtTokenGenerator.generate()
        //add it as a cookie, there is no security "success handler" after this
        Cookie cookie = TokenUtils.tokenCookie(request, token)
        response.addCookie(cookie)
        //convert to a Map to render it as json
        Map body = TokenUtils.tokenToMap(token)

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(body)
    }

    @GetMapping("/api/token/callback")
    ResponseEntity<Map> callback(HttpServletRequest request, HttpServletResponse response) {
        return token(request, response)
    }

    //returns the current userMap. Will error if not valid token or login
    @GetMapping("/api/validate")
    ResponseEntity<Map> validateToken(HttpServletRequest request, HttpServletResponse response) {
        //UserInfo userInfo = currentUser.user
        Map body = currentUser.userMap

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(body)
    }

}
