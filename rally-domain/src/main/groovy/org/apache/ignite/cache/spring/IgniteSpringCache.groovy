/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.cache.spring


import java.util.concurrent.Callable
import java.util.concurrent.locks.Lock

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.apache.ignite.springdata.proxy.IgniteCacheProxy
import org.springframework.cache.support.SimpleValueWrapper

/**
 * Spring cache implementation.
 */
@CompileStatic
class IgniteSpringCache extends SpringCache {

    AbstractCacheManager cacheMgr;

    IgniteSpringCache(IgniteCacheProxy<Object, Object> cache, AbstractCacheManager mgr) {
        super(cache, mgr)
        cacheMgr = mgr
    }

    /** {@inheritDoc} */
    @Override public <T> T get(final Object key, final Callable<T> valLdr) {
        IgniteCacheProxy<Object, Object> cache = (IgniteCacheProxy<Object, Object>)getNativeCache()
        Object val = cache.get(key);

        if (val == null) {
            Lock lock = cacheMgr.getSyncLock(cache.getName(), key);

            lock.lock();

            try {
                val = cache.get(key);

                if (val == null) {
                    try {
                        T retVal = valLdr.call();

                        val = wrapNullLocal(retVal);

                        cache.put(key, val);
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


    @CompileDynamic
    Object unwrapNullLocal(Object val) {
        return NULL.equals(val) ? null : val;
    }

    @CompileDynamic
    public <T> Object wrapNullLocal(T val) {
        return val == null ? NULL : val;
    }

}
