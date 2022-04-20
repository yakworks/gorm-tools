/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.rest.okta

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.pac4j.oidc.profile.OidcProfile

/**
 * Implementation of OidcProfile for okta openid connect.
 * Mainly required so that we can override the getId() method.
 */
@CompileStatic
@Slf4j
class OktaProfile extends OidcProfile {

    /**
     * Override to return username. Spring security rest expects username to be available under OidcProfile.id.
     * But okta sends some unique id as profile.id which breaks spring security rest
     * So grab username from email.
     */
    @Override
    public String getId() {
        return getUsername()
    }

    @Override
    public String getUsername() {
        String userName = (String) getAttribute("email")
        log.debug("OKTA Username email is $userName ")
        return userName
    }

    public String getPreferredUsername() {
        String userName = (String)getAttribute("preferred_username")
        log.debug("OKTA Username preferred_username is $userName ")
        return userName
    }
}
