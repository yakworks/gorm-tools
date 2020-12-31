/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api

import groovy.transform.CompileDynamic

import gorm.tools.mango.MangoDetachedCriteria

/**
 * a trait with statics for gorm domain entities that delegates the calls to the repository
 * which should implement the QueryMangoEntityApi
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileDynamic
trait QueryMangoEntity<D> {

    //@Transient
    //static List<String> qSearchIncludes = []

    // static abstract GormRepo<D> getRepo()

    /**
     * Builds detached criteria for repository's domain based on mango criteria language and additional optional criteria
     * call get or list on returned object to fire it
     * @param params the mango criteria language map
     * @param closure optional closure
     * @return
     */
    static MangoDetachedCriteria<D> query(Map params = [:], @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {
        ((QueryMangoEntityApi)getRepo()).query(params, closure)
    }

    /**
     * Builds detached criteria for domain, call get or list on it.
     *
     * @param closure
     * @return a DetachedCriteria instance
     */
    static MangoDetachedCriteria<D> query(@DelegatesTo(MangoDetachedCriteria) Closure closure) {
        ((QueryMangoEntityApi)getRepo()).query([:], closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    static List<D> queryList(Map params = [:], @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {
        ((QueryMangoEntityApi)getRepo()).queryList(params, closure)
    }
}
