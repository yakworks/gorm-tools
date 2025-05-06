/*
* Copyright 2002-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

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

    @GetMapping("/hbars")
    String hbars() {
        "hbars"
    }

    @GetMapping("/user")
    String userInfo(ModelMap model, @CurrentSecurityContext SecurityContext secContext) {
        model.addAttribute('user', secContext.authentication.details as UserInfo)
        "userInfo"
    }

    @GetMapping("/spring")
    String spring(ModelMap model) {
        model.addAttribute('info', 'test info')
        "spring/about"
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

}
