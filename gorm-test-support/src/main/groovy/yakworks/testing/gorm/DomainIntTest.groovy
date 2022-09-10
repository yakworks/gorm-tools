/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

import groovy.transform.CompileStatic

import org.springframework.cache.CacheManager

import gorm.tools.beans.AppCtx
import yakworks.testing.gorm.integration.DataIntegrationTest

/**
 * core integration test trait that consolodated the traits
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
