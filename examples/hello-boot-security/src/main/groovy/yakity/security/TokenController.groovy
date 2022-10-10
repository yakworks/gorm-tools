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


import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
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

    @PostMapping("/token.txt")
    String token() {
        return tokenGenerator.genererate().tokenValue
    }

    @PostMapping("/token")
    ResponseEntity<Map> apiLogin() {
        Map body = [
            token_type: 'Bearer',
            access_token: tokenGenerator.genererate().tokenValue,
            "expires_in":3600
        ]
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(body);
    }

    @PostMapping("/api/wtf")
    Map wtf() {
        return [access_token: tokenGenerator.genererate().tokenValue]
    }

}
