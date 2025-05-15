/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.boot

import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic

import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

import com.github.benmanes.caffeine.cache.Caffeine
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.spring.cache.HazelcastCacheManager
import yakworks.spring.hazelcast.HazelCacheManager

//@AutoConfiguration(after = org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration)
@Configuration(proxyBeanMethods = false)
@Lazy(false)
@CompileStatic
class CacheMgrConfig {

    // @Bean
    // HazelcastConfigCustomizer hazelcastCustomizer() {
    //     return (Config config) -> {
    //         config.getNetworkConfig().getRestApiConfig().setEnabled(true)
    //     } as HazelcastConfigCustomizer
    // }

    @Configuration
    //@Profile("!test")
    static class MainCacheBeans {

        @Bean
        Caffeine caffeineConfig() {
            return Caffeine.newBuilder()
                .initialCapacity(200)
                .recordStats()
                .expireAfterWrite(5, TimeUnit.MINUTES);
            //.maximumSize(500)
            //.weakKeys()
        }

        /** local cache is caffiene when we dont need it distributed*/
        @Bean
        @Primary
        //set as primary, or else spring will error about two cache managers
        CaffeineCacheManager cacheManager(Caffeine caffeine) {
            CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
            caffeineCacheManager.setCaffeine(caffeine);
            return caffeineCacheManager;
        }

        @Bean
        HazelcastCacheManager hazelCacheManager(HazelcastInstance instance) {
            //var hcm = new HazelcastCacheManager(instance)
            var hcm = new HazelCacheManager(instance)
            //can be set from app.yml too, see https://docs.hazelcast.com/hazelcast/5.5/spring/add-caching
            hcm.defaultLockTimeout = 5_000L //in millis
            //hcm.lockTimeoutMap['orgApiList'] = 10_000L //in millis
            //setup a default cache, not really needed, just testing
            //hcm.getCache('orgApiList')
            return hcm
        }

    }

    //@Configuration
    //@Profile("test")
    // static class TestCacheBeans {
    //
    //     @Bean
    //     @Primary
    //     CacheManager cacheManager() {
    //         return new NoOpCacheManager();
    //     }
    //
    //     @Bean
    //     CacheManager hazelCacheManager() {
    //         return new NoOpCacheManager();
    //     }
    //
    //
    // }
}
