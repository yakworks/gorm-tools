/*
 * Copyright 2013-2016 Alvaro Sanchez-Mariscal <alvaro.sanchezmariscal@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package yakworks.security.rest.token

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.http.MediaType

import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.bearer.BearerTokenReader

/**
 * Finds the bearer token and if not found tries the for token, like github , for our stored tokens and in Basic
 */
@Slf4j
@CompileStatic
class HeaderTokenReader extends BearerTokenReader {

    /**
     * find access token in header.
     * If its in something other than Bearer then the details prop will have a value of "store"
     */
    @Override
    AccessToken findToken(HttpServletRequest request) {
        log.debug "Looking for bearer token in Authorization header, query string or Form-Encoded body parameter"

        //didn't find it so try token and basic
        AccessToken accessToken
        String authHeader = request.getHeader('Authorization')

        String lowcaseHeader = authHeader?.toLowerCase()
        //check lowercase bearer
        if (lowcaseHeader?.startsWith('bearer') && authHeader.length()>=7) {
            log.debug "Found token in Authorization header with [token] key"
            accessToken = new AccessToken(authHeader.substring(7))
            // TODO implement this
            // accessToken.setDetails('jwt')
        } else if (lowcaseHeader?.startsWith('token') && authHeader.length()>=7) {
            log.debug "Found token in Authorization header with [token] key"
            accessToken = new AccessToken(authHeader.substring(6))
        } else if (lowcaseHeader?.startsWith("basic")) {
            log.debug "Found token in password for basic auth"
            // Authorization: Basic base64credentials
            String base64Credentials = authHeader.substring("Basic".length()).trim()
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials)
            String credentials = new String(credDecoded, "UTF-8")
            // credentials = username:password
            final String[] values = credentials.split(":", 2)
            //throw away use as its we just need token in password
            accessToken = new AccessToken( values[1])
        } else if (isFormEncoded(request) && request.method != 'GET') {
            log.debug "Found bearer token in request body"
            String tokenValue = request.parameterMap['access_token']?.first()
            if(tokenValue) accessToken = new AccessToken( tokenValue )
        } else {
            log.debug "No token found"
        }
        return accessToken
    }

    boolean isFormEncoded(HttpServletRequest servletRequest) {
        servletRequest.contentType && MediaType.parseMediaType(servletRequest.contentType).isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)
    }
}
