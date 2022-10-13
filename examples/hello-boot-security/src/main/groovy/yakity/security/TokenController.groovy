/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yakity.security

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import yakworks.security.spring.token.JwtTokenGenerator

/**
 * A controller for the token resource.
 *
 * @author Josh Cummings
 */
@RestController
@CompileStatic
class TokenController {

    @Autowired JwtTokenGenerator tokenGenerator
    @Value('${grails.serverURL:""}')
    String serverURL

    @PostMapping("/token.txt")
    String token() {
        return tokenGenerator.genererate().tokenValue
    }

    @PostMapping("/token")
    ResponseEntity<Map> apiLogin(HttpServletResponse response) {
        String tokenVal = tokenGenerator.genererate().tokenValue
        Map body = [
            token_type: 'Bearer',
            access_token: tokenVal,
            "expires_in":3600
        ]

        Cookie cookie = jwtCookie(tokenVal)
        response.addCookie(cookie)

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(body);
    }

    @PostMapping("/api/wtf")
    Map wtf() {
        return [access_token: tokenGenerator.genererate().tokenValue]
    }

    protected Cookie jwtCookie(String tokenValue) {
        Cookie jwtCookie = new Cookie( 'jwt', tokenValue )
        //FIXME some hard coded values to get it working
        jwtCookie.maxAge = 3600
        jwtCookie.path = '/'
        jwtCookie.setHttpOnly(httpOnly())
        if ( httpOnly() ) {
            jwtCookie.setSecure(true)
        }
        jwtCookie
    }

    protected boolean httpOnly() {
        serverURL?.startsWith('https')
    }

}
