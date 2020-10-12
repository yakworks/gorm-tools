/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api

import javax.persistence.Transient

import groovy.transform.CompileStatic

import grails.gorm.DetachedCriteria

/**
 * a trait with statics for gorm domain entities that delegates the calls to the repository
 * which should implement the QueryMangoEntityApi
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait QueryMangoEntity<D> {

    @Transient
    static List<String> qSearchIncludes = []

    static abstract getRepo()

    static DetachedCriteria<D> buildCriteria(Map params = [:], @DelegatesTo(DetachedCriteria) Closure closure = null) {
        ((QueryMangoEntityApi)getRepo()).buildCriteria(params, closure)
    }

    static DetachedCriteria<D> buildCriteria(@DelegatesTo(DetachedCriteria) Closure closure) {
        ((QueryMangoEntityApi)getRepo()).buildCriteria([:], closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    static List query(Map params = [:], @DelegatesTo(DetachedCriteria) Closure closure = null) {
        ((QueryMangoEntityApi)getRepo()).query(params, closure)
    }
}
