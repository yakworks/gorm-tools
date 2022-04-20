/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tenant

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.multitenancy.AllTenantsResolver
import org.grails.datastore.mapping.multitenancy.TenantResolver
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy

import gorm.tools.security.domain.AppUser
import gorm.tools.security.services.SecService
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@CompileStatic
class UserTenantResolver implements TenantResolver { //AllTenantsResolver {

    @Autowired
    @Lazy
    SecService<AppUser> secService

    @Override
    Serializable resolveTenantIdentifier() throws TenantNotFoundException {
        Long orgId = secService.user?.orgId
        if ( orgId ) {
            return orgId
        }
        throw new TenantNotFoundException("Tenant could not be resolved, either not logged in or orgId is not assign properly to User")
    }

    Org getOrg(){
        // if(!org) org = Org.get(secService.user.orgId)
        return Org.get(secService.user.orgId)
    }

    /**
     * is the current user a customer.
     * Looks at the org for the user vs whats on the jwt token
     */
    boolean isCustomer(){
        getOrg().type == OrgType.Customer
    }

    // @Override
    // Iterable<Serializable> resolveTenantIds() {
    //     User.withTransaction(readOnly: true) {
    //         new DetachedCriteria(User)
    //             .distinct('username')
    //             .list()  as Iterable<Serializable>
    //     }
    // }
}
