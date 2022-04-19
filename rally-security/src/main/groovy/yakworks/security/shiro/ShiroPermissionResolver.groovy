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

import groovy.transform.CompileStatic

/**
 * Implement this interface and register the class as the 'shiroPermissionResolver' bean.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
interface ShiroPermissionResolver {
    /**
     * Find the permissions granted to the specified user, e.g. using GORM.
     *
     * @param username the username
     * @return zero or more permissions.
     */
    Set<String> resolvePermissions(String username)
}
