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
     * @param entityClass the base entity class
     * @param params to process into QueryArgs
     * @param closure extra criterai closure
     * @return the detached criteria to call list or get on
     */
    public <D> MangoDetachedCriteria<D> query(Class<D> entityClass, Map params, Closure closure)

    /**
     *  Builds detached criteria for repository's domain based on mango criteria language
     *
     * @param entityClass the base entity class
     * @param qargs the QueryArgs with the prepared criteria in it.
     * @param closure extra criterai closure
     * @return the detached criteria to call list or get on
     */
    public <D> MangoDetachedCriteria<D> query(Class<D> entityClass, QueryArgs qargs, Closure closure)

    /**
     * shortcut to call query and then list with the pager fields in params
     */
    public <D> List<D> queryList(Class<D> entityClass, Map params, Closure closure)

    public <D> List<D> queryList(Class<D> entityClass, QueryArgs qargs, Closure closure)
}
