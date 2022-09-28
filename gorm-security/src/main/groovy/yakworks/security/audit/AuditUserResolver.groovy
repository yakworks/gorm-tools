/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.audit

import groovy.transform.CompileStatic

import yakworks.security.UserInfo

/**
 * Interface for service that audit-stamp uses to get what it needs
 */
@CompileStatic
interface AuditUserResolver {
    /**
     * @return the current actor
     */
    Serializable getCurrentUserId()

    /**
     * @return the current request URI or null if no active request
     */
    UserInfo getUserInfo(Serializable userId)
}
