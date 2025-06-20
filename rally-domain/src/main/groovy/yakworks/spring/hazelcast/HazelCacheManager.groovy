/*
* Copyright 2008-2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring.hazelcast

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import com.hazelcast.spring.cache.HazelcastCacheManager
import yakworks.util.ReflectionUtils

/**
 * Spring-related {@link CacheManager} implementation for Hazelcast.
 */
@Slf4j
@SuppressWarnings(['WeakerAccess', 'ExplicitHashMapInstantiation'])
@CompileStatic
public class HazelCacheManager extends HazelcastCacheManager {

    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>();
    /**
     * Default cache tryLock wait timeout. Apply to all caches.
     */
    long defaultLockTimeout;

    /**
     * Holds cache specific tryLock wait timeout. Override defaultLockTimeout for specified caches.
     */
    Map<String, Long> lockTimeoutMap = new HashMap<String, Long>();

    public HazelCacheManager() {}

    public HazelCacheManager(HazelcastInstance hazelcastInstance) {
        super(hazelcastInstance);
    }

    //complete replacment since we do so much work. no way to extend it since we replace with HazelCache.
    //we add the lockTimeout
    @Override
    public Cache getCache(String name) {
        log.debug "************** getCache name $name"
        Cache cache = caches.get(name);
        if (cache == null) {
            IMap<Object, Object> map = hazelcastInstance.getMap(name);
            cache = new HazelCache(map);

            long cacheTimeout = calculateCacheReadTimeout(name);
            cache.setReadTimeout(cacheTimeout);

            long cacheLockTimeout = calculateCacheLockTimeout(name);
            cache.setLockTimeout(cacheLockTimeout);

            Cache currentCache = caches.putIfAbsent(name, cache);
            if (currentCache != null) {
                cache = currentCache;
            }
        }
        return cache;
    }

    ConcurrentMap<String, Cache> getCaches(){
        //groovy not abel to access even with CompileDynamic
        //(ConcurrentMap<String, Cache>) ReflectionUtils.getPrivateFieldValue(HazelcastCacheManager, "caches", this)
        return caches
    }

    //Override replace super since its private
    private long calculateCacheLockTimeout(String name) {
        Long timeout = getLockTimeoutMap().get(name);
        return timeout == null ? defaultLockTimeout : timeout;
    }

    private long calculateCacheReadTimeout(String name) {
        Long timeout = getReadTimeoutMap().get(name);
        return timeout == null ? defaultReadTimeout : timeout;
    }

    Cache create(String name) {
        return null;
    }
}
