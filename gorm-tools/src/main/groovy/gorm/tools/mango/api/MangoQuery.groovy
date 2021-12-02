/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api

import gorm.tools.mango.MangoDetachedCriteria

/**
 * Interface to be implemented by a bean
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
interface MangoQuery {

    /**
     * Builds detached criteria for repository's domain based on mango criteria language and additional criteria
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    public <D> MangoDetachedCriteria<D> query(Class<D> domainClass, Map params, Closure closure)

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    public <D> List<D> queryList(Class<D> domainClass, Map params, Closure closure)

    public <D> List<D> queryList(Class<D> domainClass, QueryArgs qargs, Closure closure)
}
