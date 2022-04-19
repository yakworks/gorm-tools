/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tenant


import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.metamap.MetaMapEntityService
import gorm.tools.security.domain.AppUser
import gorm.tools.security.services.SecService
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

/**
 * Customer security service for unique handling.
 * PROOF OF CONCEPT
 */
// @RequestScope
// @Service
@CompileStatic
class UserRequest {

    @Autowired SecService<AppUser> secService
    @Autowired MetaMapEntityService metaMapEntityService

    AppUser user

    /**
     * Org the user is logged in as
     */
    Org org

    // @PostConstruct
    // void init(){
    //     org = Org.get(secService.user.orgId)
    //     user = secService.user
    // }

    Org getOrg(){
        if(!org) org = Org.get(secService.user.orgId)
        return org
    }

    /**
     * is the current user a customer.
     * Looks at the org for the user vs whats on the jwt token
     */
    boolean isCustomer(){
        getOrg().type == OrgType.Customer
    }

    /**
     * Initial config returned to ui, such as the current user info, sidebar menu config, etc..
     */
    Map getUserMap() {
        List incs = ['username', 'name', 'email', 'orgId']
        Map userMap = metaMapEntityService.createMetaMap(secService.user, incs).clone() as Map
        if (isCustomer()) userMap.put('isCustomer', true)
        return userMap
    }

}
