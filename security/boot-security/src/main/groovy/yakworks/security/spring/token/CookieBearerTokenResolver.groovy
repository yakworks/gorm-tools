/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver

/**
 * Injected in BearerTokenAuthenticationFilter,
 * Wraps the DefaultBearerTokenResolver that only checks the Authorization header by default.
 * If default check in auth header is null then will look for cookie too.
 */
@CompileStatic
class CookieBearerTokenResolver implements BearerTokenResolver {

    DefaultBearerTokenResolver defaultBearerTokenResolver = new DefaultBearerTokenResolver()

    @Override
    String resolve(HttpServletRequest request) {
        String token = defaultBearerTokenResolver.resolve(request)
        //no token? check cookie before returning
        if(!token) token = findTokenCookie(request)
        return token
    }

    String findTokenCookie(HttpServletRequest request) {
        Cookie cookie = request.getCookies()?.find { Cookie cookie -> cookie.name.equalsIgnoreCase(TokenUtils.COOKIE_NAME) }
        return cookie?.value
    }
}
