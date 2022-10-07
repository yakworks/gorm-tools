/*
 * Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
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

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.log.LogMessage
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Extends the UsernamePasswordAuthenticationFilter to allow username/password to be passed as JSON in the body of the POST
 * `http POST localhost:8080/login username=Joe password=123`
 */
@CompileStatic
class JsonUsernamePasswordLoginFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    ObjectMapper objectMapper;

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/api/login",
        "POST");

    JsonUsernamePasswordLoginFilter(ObjectMapper objectMapper) {
        super();
        this.objectMapper = objectMapper
        setRequiresAuthenticationRequestMatcher(DEFAULT_ANT_PATH_REQUEST_MATCHER)
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        //first check super that checks ant filter
        if (super.requiresAuthentication(request, response)) {
            if (request.contentType.startsWith("application/json") && request.contentLength > 0) {
                return true
            }
        }
        return false
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        Map<String, Object> body = objectMapper.readValue(request.inputStream, Map.class);
        String username = body[usernameParameter]
        String password = body[passwordParameter]
        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(username,
            password);
        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    // @Override
    // protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
    //                                         Authentication authResult) throws IOException, ServletException {
    //     super.successfulAuthentication(request, response, chain, authResult)
    //     //continue on so it can go to controller to return the token
    //     chain.doFilter(request, response);
    // }
}
