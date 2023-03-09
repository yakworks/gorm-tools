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

import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.RequestContextHolder

import yakworks.security.user.UserInfo

/**
 * Controller for "/spring".
 *
 * @author Joe Grandja
 */
@Controller
@CompileStatic
// @RequestMapping(value="/")
class IndexController {

    @GetMapping("/")
    String home() {
        // var auth = SecurityContextHolder.getContext().getAuthentication()
        // println "************************************** user ${auth.principal}"
        "index"
    }

    @GetMapping("/spring")
    String spring() {
        "spring/index.html"
    }

    @GetMapping("/about")
    @ResponseBody String about(ModelMap model) {
        model.addAttribute('info', 'test info')
        "just a string"
    }

    @GetMapping("/test")
    void test(HttpServletResponse response) {
        response.setContentType("text/csv")
        response.writer.write("it works")
    }

    @RequestMapping("/user-info")
    @ResponseBody UserInfo userInfo(@CurrentSecurityContext SecurityContext secContext) {
        return secContext.authentication.details as UserInfo
    }

    @GetMapping("/samlSuccess")
    @ResponseBody String samlSuccess(ModelMap model) {
        model.addAttribute('info', 'test info')
        "samlSuccess"
    }
}
