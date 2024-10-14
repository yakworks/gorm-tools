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
import yakworks.commons.map.Maps
import yakworks.gorm.api.DefaultCrudApi
import yakworks.gorm.api.IncludesProps
import yakworks.meta.MetaMapList
import yakworks.rally.orgs.model.Org
import yakworks.security.user.CurrentUser

@Slf4j
@Component
@CompileStatic
class OrgCrudApi extends DefaultCrudApi<Org> {

    @Inject CurrentUser currentUser
    @Inject OrgCrudApi self

    OrgCrudApi() {
        super(Org)
    }

    /*
     How to make sure use can't run to many requests
     1. Check user setting for maxRequests
     2.
     */
    //, cacheManager = "hazelCacheManager"

    @Cacheable(
        cacheNames="orgApiList",
        key="{@currentUser.getUserId(), #qParams.toString()}",
        sync=true
    )
    //" + #includesKeys.toString()")
    @Override
    Pager list(Map qParams){
        assert self
        log.debug("no cache hit")
        println "*********************NO HIT****************************"
        if(qParams.sleep) sleep(60000)
        super.list(qParams)
    }

    @Override
    Pager createPagerResult(Pager pager, Map qParams, List dlist, List<String> includesKeys) {
        pager = super.createPagerResult(pager, qParams, dlist, includesKeys)
        //we clone this here so it can be cached with all the associations initialized
        pager.data = Maps.clone(pager.data ) as List<Map>
        return pager
    }

}
