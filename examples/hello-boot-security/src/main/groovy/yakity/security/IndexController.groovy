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
package yakity.security

import groovy.transform.CompileStatic

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Controller for "/spring".
 *
 * @author Joe Grandja
 */
@Controller
@CompileStatic
class IndexController {

    @GetMapping("/")
    String home() {
        var auth = SecurityContextHolder.getContext().getAuthentication()
        // println "************************************** user ${auth.principal}"
        "index.html"
    }

    @GetMapping("/spring")
    String index() {
        "spring/index.html"
    }

    @GetMapping("/about")
    String about(ModelMap model) {
        model.addAttribute('info', 'test info')
        "about" //handlebars
    }

    @RequestMapping("/user-info")
    public String saml(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal, Model model) {
        model.addAttribute("name", principal.getName());
        model.addAttribute("emailAddress", principal.getFirstAttribute("email"));
        model.addAttribute("userAttributes", principal.getAttributes());
        return "saml.html";
    }
}
