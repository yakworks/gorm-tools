/*
 * Copyright 2002-2016 the original author or authors.
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
package yakworks.rally.api

import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import yakworks.security.user.UserInfo

/**
 * Controller for "/spring".
 */
@RestController
@RequestMapping("/rally/smoke")
@CompileStatic
@Slf4j
class SmokeController {

    @GetMapping()
    String home() {
        "hello"
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    Map smokeTestPost(@RequestParam String foo,
                      @RequestParam Map<String,String> params,
                      @RequestBody Map payload) {
        assert foo == "bar"
        params + payload
    }
}
