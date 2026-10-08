/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.auditable.resolvers

import groovy.transform.CompileStatic

@CompileStatic
interface AuditRequestResolver {
    /**
     * @return the current actor
     */
    String getCurrentActor()

    /**
     * @return the current request URI or null if no active request
     */
    String getCurrentURI()
}
