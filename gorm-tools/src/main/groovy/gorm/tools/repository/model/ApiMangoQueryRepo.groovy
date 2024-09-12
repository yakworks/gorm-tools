/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.MangoQuery
import gorm.tools.mango.api.QueryArgs

/**
 * For repos and concretes classes that work on a single entity
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
interface ApiMangoQueryRepo<D> {

    Class getEntityClass()

    MangoQuery getMangoQuery()

    /**
     * Primary method. Builds detached criteria for repository's domain based on mango criteria language and additional criteria
     * Override this one in repo for any special handling
     *
     * @param queryArgs mango query args.
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    default MangoDetachedCriteria<D> query(QueryArgs queryArgs, @DelegatesTo(MangoDetachedCriteria)Closure closure) {
        getMangoQuery().query(getEntityClass(), queryArgs, closure)
    }

    default MangoDetachedCriteria<D> query(QueryArgs queryArgs) {
        query(queryArgs, null)
    }

    /**
     * Builds detached criteria for repository's domain based on mango criteria language and additional criteria
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    default MangoDetachedCriteria<D> query(Map params, @DelegatesTo(MangoDetachedCriteria)Closure closure) {
        query(QueryArgs.of(params), closure)
    }

    default MangoDetachedCriteria<D> query(Map params) {
        query(QueryArgs.of(params), null)
    }

    // default MangoDetachedCriteria<D> query(@DelegatesTo(MangoDetachedCriteria)Closure closure) {
    //     query(QueryArgs.of([:]), closure)
    // }

}
