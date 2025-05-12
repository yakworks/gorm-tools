/*
* Copyright 2008-2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring.hazelcast

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import javax.persistence.LockTimeoutException

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.cache.Cache

import com.hazelcast.map.IMap
import com.hazelcast.spring.cache.HazelcastCache

/**
 * Spring related {@link Cache} implementation for Hazelcast.
 */
@SuppressWarnings(['UnnecessaryOverridingMethod'])
@Slf4j
@CompileStatic
public class HazelCache extends HazelcastCache {

    /**
     * Lock timeout for cache value retrieval operations.
     */
    long lockTimeout

    public HazelCache(IMap<Object, Object> map) {
        super(map);
    }

    @Override
    void put(Object key, Object value) {
        log.debug("************ Cache[${getName()}] PUT for Key: ${key}, value: ${value}")
        super.put(key, value)
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = lookup(key);
        if (value != null) {
            log.debug("************ Cache[${getName()}] HIT for Key: ${key}")
            return (T) fromStoreValue(value);
        } else {
            doLock(key)
            try {
                value = lookup(key);
                if (value != null) {
                    log.debug("************ Cache[${getName()}] HIT for Key: ${key}")
                    return (T) fromStoreValue(value);
                } else {
                    return loadValue(key, valueLoader);
                }
            } finally {
                getNativeCache().unlock(key);
            }
        }
    }

    void doLock(Object key){
        if(lockTimeout){
            boolean gotLock = getNativeCache().tryLock(key, lockTimeout, TimeUnit.MILLISECONDS);
            if(!gotLock) {
                log.debug("Lock timeout on key $key")
                //TODO fire the LockTimoutException
                throw new LockTimeoutException("Lock timeout on key $key, waited ${lockTimeout / 1000}s")
            }
        } else {
            //does normal lock like it did before
            getNativeCache().lock(key);
        }
    }

    @CompileDynamic
    private Object lookup(Object key) {
        super.lookup(key)
    }

    @CompileDynamic
    private <T> T loadValue(Object key, Callable<T> valueLoader) {
        super.loadValue(key, valueLoader)
    }

    /**
     * Wrapper exception to be thrown from {@link #get(Object, Callable)}
     * in case of the value loader callback failing with an exception.
     * @since 4.3
     */
    // @SuppressWarnings("serial")
    // class LockTimoutException extends RuntimeException {
    //
    //     private final Object key;
    //     private final long lockTimeoutSeconds
    //
    //     public LockTimoutException(Object key, long lockTimeoutSeconds) {
    //         super("Lock timeout on key $key, waited ${lockTimeoutSeconds/1000}s")
    //         this.key = key
    //         this.lockTimeoutSeconds = lockTimeoutSeconds
    //     }
    //
    //     public Object getKey() {
    //         return this.key;
    //     }
    // }
}
