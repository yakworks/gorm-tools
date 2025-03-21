/*
* Copyright 2020-2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import javax.inject.Inject
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.core.AbstractOAuth2Token
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import jakarta.annotation.Nullable
import yakworks.api.HttpStatus
import yakworks.api.problem.Problem
import yakworks.api.problem.UnexpectedProblem
import yakworks.security.spring.token.generator.JwtTokenExchanger
import yakworks.security.spring.token.generator.JwtTokenGenerator
import yakworks.security.spring.token.generator.StoreTokenGenerator
import yakworks.security.user.CurrentUser

/**
 * A controller for the token resource.
 */
@RestController
@CompileStatic
@Slf4j
class TokenController {

    @Inject JwtTokenGenerator jwtTokenGenerator
    @Inject JwtTokenExchanger jwtTokenExchanger

    //used for tokenLegacy right now
    @Inject @Nullable
    StoreTokenGenerator storeTokenGenerator

    @Inject CurrentUser currentUser


    // @Value('${grails.serverURL:""}')
    // String serverURL

    // for dev and testing to make it easier to dump token into variable.
    // ex: `$ TOKEN=`http POST admin:123@localhost:8080/token.txt -b`
    @PostMapping("/oauth/token.txt")
    String tokenTxt() {
        return jwtTokenGenerator.generate().tokenValue
    }

    /**
     * Default generator for token. Follows the oauth standards.
     */
    @PostMapping("/oauth/token")
    ResponseEntity<Map> token(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String,String> params) {
        Map body
        //will pick up urn:ietf:params:oauth:grant-type:token-exchange or just token-exchange
        if(params?.grant_type?.endsWith("token-exchange")){
            body = exchangeToken(params)
        } else {
            //grant_type is password by default and the only other one supported right now
            body = generateToken(request, response)
        }
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(body)
    }

    Map generateToken(HttpServletRequest request, HttpServletResponse response){
        Jwt token = jwtTokenGenerator.generate()
        //add it as a cookie, there is no security "success handler" after this
        Cookie cookie = TokenUtils.tokenCookie(request, token)
        response.addCookie(cookie)
        //convert to a Map to render it as json
        Map body = TokenUtils.tokenToMap(token)
        return body
    }

    Map exchangeToken(Map<String,String> params ){
        String requested_subject = params.requested_subject
        assert requested_subject
        Jwt token = jwtTokenExchanger.exchange(requested_subject)
        Map body = TokenUtils.tokenToMap(token)
        //add sub to rep as well so its obvious its an exchange for new subject
        body.sub = requested_subject
        return body
    }

    @GetMapping("/oauth/token/callback")
    ResponseEntity<Map> callback(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String,String> params ) {
        return token(request, response, params)
    }

    /**
     * Basically a url for grant_type=password and storing a user token in the table
     * TODO for legacy and possible in future.  JsonUsernamePasswordLogin forwards to this right now
     * we will sunset this once we move off of having stored tokens as the default when using the login.
     * @see yakworks.security.spring.DefaultSecurityConfiguration#addJsonAuthenticationFilter
     */
    @Deprecated
    @PostMapping("/tokenLegacy")
    ResponseEntity<Map> tokenLegacy(HttpServletRequest request, HttpServletResponse response) {

        AbstractOAuth2Token token = storeTokenGenerator.generate()
        //add it as a cookie, there is no security "success handler" after this
        Cookie cookie = TokenUtils.tokenCookie(request, token)
        response.addCookie(cookie)
        //convert to a Map to render it as json
        Map body = TokenUtils.tokenToMap(token)

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(body)
    }

    //returns the current userMap. Will error if not valid token or login
    @GetMapping("/validate")
    ResponseEntity<Map> validateToken(HttpServletRequest request, HttpServletResponse response) {
        //UserInfo userInfo = currentUser.user
        Map body = currentUser.userMap

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(body)
    }


    @ExceptionHandler(Exception.class)
    def handleException(HttpServletRequest req, HttpServletResponse resp, Exception e) {
        //We dont have access to ProblemHandler here in boot-security. But use a problem to be consistent.
        Problem problem
        if(e instanceof UsernameNotFoundException ) {
            problem = Problem.of('user.notfound').status(HttpStatus.NOT_FOUND).detail(e.message)
        } else {
            problem = new UnexpectedProblem().cause(e).detail(e.message)
        }
        log.warn(e.message)
        return problem.asMap()
    }
}
