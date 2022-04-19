/* Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package yakworks.security.shiro

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.apache.shiro.SecurityUtils
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
class ShiroLogoutHandler implements LogoutHandler {
    void logout(HttpServletRequest req, HttpServletResponse res, Authentication a) {
        SecurityUtils.subject.logout()
    }
}
