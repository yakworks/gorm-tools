/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package org.apache.ignite.cache.spring


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.NearCacheConfiguration
import org.apache.ignite.springdata.proxy.IgniteNodeCacheProxy

@Slf4j
@CompileStatic
class IgniteCacheManager extends SpringCacheManager {

    Ignite igniteInstance;

    /**
     * Default cache tryLock wait timeout. Apply to all caches.
     */
    long defaultLockTimeout;

    /** {@inheritDoc} */
    @Override protected SpringCache createCache(String name) {
        CacheConfiguration<Object, Object> cacheCfg = getDynamicCacheConfiguration() != null ?
            new CacheConfiguration<>(getDynamicCacheConfiguration()) : new CacheConfiguration<>();

        NearCacheConfiguration<Object, Object> nearCacheCfg = getDynamicNearCacheConfiguration() != null ?
            new NearCacheConfiguration<>(getDynamicNearCacheConfiguration()) : null;

        cacheCfg.setName(name);

        IgniteCache<Object, Object> cache = nearCacheCfg != null
            ? igniteInstance.getOrCreateCache(cacheCfg, nearCacheCfg)
            : igniteInstance.getOrCreateCache(cacheCfg);

        IgniteSpringCache igCache =  new IgniteSpringCache(new IgniteNodeCacheProxy<>(cache), this);
        if(defaultLockTimeout) igCache.lockTimeout = this.defaultLockTimeout
        return igCache
    }

    void setIgniteInstance(Ignite inst){
        this.igniteInstance = inst
        this.igniteInstanceName = inst.name()
    }
    /** {@inheritDoc} */
    // @Override public void onApplicationEvent(ContextRefreshedEvent event) {
    //     super.onApplicationEvent(event)
    //     igniteInstance = Ignition.ignite(getIgniteInstanceName())
    //
    // }

    @Override public void destroy() {
        log.info("Ignite Cache Detroy")
    }

}
