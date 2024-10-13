/*
* Copyright 2008-2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.hazelcast

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.cache.Cache
import org.springframework.lang.Nullable

import com.hazelcast.map.IMap
import com.hazelcast.spring.cache.HazelcastCache
import yakworks.api.HttpStatus
import yakworks.api.problem.Problem

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
    public <T> T get(Object key, Callable<T> valueLoader) {
        log.debug("get ${key}")
        Object value = lookup(key);
        if (value != null) {
            return (T) fromStoreValue(value);
        } else {
            doLock(key)
            try {
                value = lookup(key);
                if (value != null) {
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
            if(!gotLock)
                //TODO fire the LockTimoutException
                throw Problem.of('error.query.duplicate')
                    .detail("Timeout waiting for identical query to finish")
                    .status(HttpStatus.TOO_MANY_REQUESTS).toException()
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
    @SuppressWarnings("serial")
    class LockTimoutException extends RuntimeException {

        @Nullable
        private final Object key;

        public LockTimoutException(@Nullable Object key, Throwable ex) {
            super(String.format("Value for key '%s' could not be loaded using '%s'", key), ex);
            this.key = key;
        }

        @Nullable
        public Object getKey() {
            return this.key;
        }
    }
}
