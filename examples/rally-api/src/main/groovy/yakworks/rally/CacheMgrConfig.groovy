/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally

import groovy.transform.CompileStatic

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.hazelcast.HazelcastConfigCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy

import com.hazelcast.config.Config
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.spring.cache.HazelcastCacheManager
import yakworks.spring.hazelcast.HazelCacheManager

@AutoConfiguration(after = org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration)
@CompileStatic
class CacheMgrConfig {

    @Bean @Lazy(false)
    //@DependsOn(["cacheManager"])
    HazelcastCacheManager hazelCacheManager(HazelcastInstance instance) {
        //var hcm = new HazelcastCacheManager(instance)
        var hcm = new HazelCacheManager(instance)
        //can be set from app.yml too, see https://docs.hazelcast.com/hazelcast/5.5/spring/add-caching
        hcm.lockTimeoutMap['orgApiList'] = 10_000L //in millis
        //setup some default caches
        hcm.getCache('orgApiList')

        return hcm
    }

    // @Bean
    // HazelcastConfigCustomizer hazelcastCustomizer() {
    //     return (Config config) -> {
    //         config.getNetworkConfig().getRestApiConfig().setEnabled(true)
    //     } as HazelcastConfigCustomizer
    // }

}
