/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.integration

import groovy.transform.CompileStatic

import org.springframework.cache.CacheManager

import yakworks.spring.AppCtx

/**
 * core integration test trait that consolodates the traits to include security.
 */
@CompileStatic
trait DomainIntTest implements DataIntegrationTest, SecuritySpecHelper {

    void clearAppConfigCache(){
        getCacheManager()?.getCache("appConfig")?.clear()
    }

    CacheManager getCacheManager(){
        AppCtx.get('cacheManager', CacheManager)
    }
}
