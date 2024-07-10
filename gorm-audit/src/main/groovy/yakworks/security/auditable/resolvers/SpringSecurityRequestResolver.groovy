/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.auditable.resolvers
/**
 * Default resolver that uses the SpringSecurityService principal if available
 */
class SpringSecurityRequestResolver extends DefaultAuditRequestResolver {
    def springSecurityService

    @Override
    String getCurrentActor() {
        springSecurityService?.currentUser?.toString() ?: super.getCurrentActor()
    }
}
