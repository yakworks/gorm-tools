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
import gorm.tools.mango.api.QueryArgs
import yakworks.commons.map.Maps
import yakworks.gorm.api.DefaultCrudApi
import yakworks.gorm.api.IncludesKey
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
    String cacheName = 'WTF'

    @Cacheable(
        value='crudApi:list',
        cacheManager = "hazelCacheManager",
        key="{@currentUser.getUserId(), #qParams.toString(), #root.target.entityClass.simpleName}",
        sync=true
    )
    @Override
    Pager list(Map qParams, URI uri){
        log.debug("********************* list no cache hit")
        //println "*********************NO HIT****************************"
        if(qParams.sleep) sleep((qParams.sleep as Integer) * 1000)
        super.list(qParams, uri)
    }

    @Cacheable(
        value='crudApi:list',
        cacheManager = "hazelCacheManager",
        key="{@currentUser.getUserId(), #qParams.toString(), #root.target.entityClass.simpleName}",
        sync=true
    )
    @Override
    Pager pickList(Map qParams, URI uri){
        //log.debug("********************* pickList no cache hit")
        //super.pickList(qParams)
        Pager pager = Pager.of(qParams)
        QueryArgs qargs = createQueryArgs(pager, qParams, uri)
        List pickSelect = includesConfig.findByKeys(entityClass, [IncludesKey.picklist, IncludesKey.stamp])
        qargs.select(pickSelect)
        List dlist = getApiCrudRepo().query(qargs, null).pagedList(qargs.pager)
        return createPagerResult(pager, qParams, dlist, [])
    }

    @Override
    Pager createPagerResult(Pager pager, Map qParams, List dlist, List<String> includes) {
        pager = super.createPagerResult(pager, qParams, dlist, includes)
        //we clone this here so it can be cached with all the associations initialized
        pager.data = Maps.clone(pager.data ) as List<Map>
        return pager
    }

}
