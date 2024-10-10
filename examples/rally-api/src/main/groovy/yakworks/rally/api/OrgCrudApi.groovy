/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import javax.inject.Inject

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

import gorm.tools.beans.Pager
import yakworks.gorm.api.DefaultCrudApi
import yakworks.rally.orgs.model.Org
import yakworks.security.user.CurrentUser

@Slf4j
@Component
@CompileStatic
class OrgCrudApi extends DefaultCrudApi<Org> {

    @Inject CurrentUser currentUser

    OrgCrudApi() {
        super(Org)
    }

    /*
     How to make sure use can't run to many requests
     1. Check user setting for maxRequests
     2.
     */
    //, cacheManager = "hazelCacheManager"
    //@Cacheable(cacheNames="orgCrudApiList", key="{#qParams.toString(),#includesKeys.toString()}") //" + #includesKeys.toString()")
    @Override
    Pager list(Map qParams, List<String> includesKeys){
        log.debug("no cache hit")
        super.list(qParams,includesKeys )
    }
}
