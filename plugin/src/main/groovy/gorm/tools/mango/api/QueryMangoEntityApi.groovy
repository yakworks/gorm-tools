/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import grails.gorm.DetachedCriteria

/**
 * For repos and concretes classes that work on a single entity
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait QueryMangoEntityApi<D> {

    abstract Class getEntityClass()

    @Autowired
    @Qualifier("mangoQuery")
    MangoQuery mangoQuery

    /**
     * Builds detached criteria for repository's domain based on mango criteria language and additional criteria
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    DetachedCriteria<D> buildCriteria(Map params = [:], Closure closure = null) {
        getMangoQuery().buildCriteria(getEntityClass(), params, closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    List query(Map params = [:], Closure closure = null) {
        getMangoQuery().query(getEntityClass(), params, closure)
    }
}
