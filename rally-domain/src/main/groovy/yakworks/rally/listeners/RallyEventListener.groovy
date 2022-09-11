/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.listeners

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener

import gorm.tools.repository.events.BeforePersistEvent
import jakarta.annotation.Nullable
import yakworks.rally.orgs.model.Company
import yakworks.security.SecService
import yakworks.security.gorm.model.AppUser

/**
 * temp in place to assign defualt orgId to user as Company default (2)
 */
@Slf4j
@CompileStatic
class RallyEventListener {

    @Autowired @Nullable
    SecService<AppUser> secService

    /**
     * Assign user.org id to, logged in user's orgid if not null, or default orgid - 2
     */
    @EventListener
    void beforeUserPersist(BeforePersistEvent<AppUser> event) {
        AppUser user = event.entity
        if(user.orgId == null) {
            if(secService.isLoggedIn() && secService.user?.orgId != null) {
                user.orgId = secService.user.orgId
            } else {
                user.orgId = Company.DEFAULT_COMPANY_ID
            }
        }
    }
}
