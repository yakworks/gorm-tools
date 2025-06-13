/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package org.apache.ignite.cache.spring

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import javax.persistence.LockTimeoutException

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.ignite.springdata.proxy.IgniteCacheProxy

/**
 * Spring cache implementation.
 */
@Slf4j
@CompileStatic
class IgniteSpringCache extends SpringCache {

    AbstractCacheManager cacheMgr;
    IgniteCacheProxy<Object, Object> cacheProxy

    /**
     * Lock timeout for cache value retrieval operations.
     */
    long lockTimeout

    IgniteSpringCache(IgniteCacheProxy<Object, Object> cache, AbstractCacheManager mgr) {
        super(cache, mgr)
        cacheProxy = cache
        cacheMgr = mgr
    }

    /** {@inheritDoc} */
    @Override public <T> T get(final Object key, final Callable<T> valLdr) {

        Object val = cacheProxy.get(key);

        if (val == null) {
            Lock lock = doLock(cacheProxy, key)

            try {
                val = cacheProxy.get(key);

                if (val == null) {
                    try {
                        T retVal = valLdr.call();

                        val = wrapNullLocal(retVal);

                        cacheProxy.put(key, val);
                    }
                    catch (Exception e) {
                        throw new ValueRetrievalException(key, valLdr, e);
                    }
                }
            }
            finally {
                lock.unlock();
            }
        }

        return (T)unwrapNullLocal(val);
    }

    Lock doLock(IgniteCacheProxy<Object, Object> cache, Object key){
        if(lockTimeout){
            //boolean gotLock = getNativeCache().tryLock(key, lockTimeout, TimeUnit.MILLISECONDS);
            Lock lock = cacheMgr.getSyncLock(cache.getName(), key);
            boolean gotLock = lock.tryLock(lockTimeout, TimeUnit.MILLISECONDS);

            if(!gotLock) {
                log.debug("Lock timeout on key $key")
                //TODO fire the LockTimoutException
                throw new LockTimeoutException("Lock timeout on key $key, waited ${lockTimeout / 1000}s")
            }
            return lock
        } else {
            //does normal lock like it did before
            Lock lock = cacheMgr.getSyncLock(cache.getName(), key);
            lock.lock()
            return lock
        }
    }

    @CompileDynamic
    Object unwrapNullLocal(Object val) {
        return super.unwrapNull(val)
    }

    @CompileDynamic
    public <T> Object wrapNullLocal(T val) {
        return super.wrapNull(val)
    }

}
