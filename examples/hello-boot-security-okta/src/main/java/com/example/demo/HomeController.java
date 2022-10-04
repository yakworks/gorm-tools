package com.example.demo;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        var cts = SecurityContextHolder.getContext().getAuthentication();
        return "index";
    }


    // @RequestMapping("/okta")
    // public String okta(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal, Model model) {
    //     model.addAttribute("name", principal.getName());
    //     model.addAttribute("emailAddress", principal.getFirstAttribute("email"));
    //     model.addAttribute("userAttributes", principal.getAttributes());
    //     return "okta";
    // }
}
