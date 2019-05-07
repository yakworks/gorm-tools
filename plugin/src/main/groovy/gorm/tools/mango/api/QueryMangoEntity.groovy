/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api

import grails.gorm.DetachedCriteria
import groovy.transform.CompileStatic

import javax.persistence.Transient

/**
 * a trait with statics for gorm domain entities that delegates the calls to the repository
 * which should implement the QueryMangoEntityApi
 */
@CompileStatic
trait QueryMangoEntity {

    @Transient
    static List<String> quickSearchFields = []

    static abstract getRepo()

    static DetachedCriteria buildCriteria(Map params = [:], Closure closure = null) {
        ((QueryMangoEntityApi)getRepo()).buildCriteria(params, closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    static List query(Map params = [:], Closure closure = null) {
        ((QueryMangoEntityApi)getRepo()).query(params, closure)
    }
}
