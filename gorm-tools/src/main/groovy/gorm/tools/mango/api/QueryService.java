/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api;

import groovy.lang.Closure;
import groovy.transform.CompileStatic;

import gorm.tools.beans.Pager;
import gorm.tools.mango.MangoDetachedCriteria;

import java.util.List;
import java.util.Map;

/**
 * Interface to be implemented by a bean
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
public interface QueryService<D> {

    Class<D> getEntityClass();

    /**
     * Builds detached criteria for repository's domain based on mango criteria language and additional criteria
     *
     * @param params to process into QueryArgs
     * @param closure extra criterai closure
     * @return the detached criteria to call list or get on
     */
    default MangoDetachedCriteria<D> query(Map params, Closure closure){
        return query(QueryArgs.of(params), closure);
    }

    /**
     *  Builds detached criteria for repository's domain based on mango criteria language
     ** @param qargs the QueryArgs with the prepared criteria and props in it.
     * @param closure extra criterai closure
     * @return the detached criteria to call list or get on
     */
    MangoDetachedCriteria<D> query(QueryArgs qargs, Closure closure);

    /**
     * passes on to the mangoBuilder, allows sub-classes to override and modify
     */
    MangoDetachedCriteria<D> createCriteria(QueryArgs qargs, Closure applyClosure);

    /**
     * passes on to the mangoBuilder, allows sub-classes to override and modify
     */
    void applyCriteria(MangoDetachedCriteria<D> mangoCriteria);

    /**
     * shortcut to call query and then list with the pager fields in params
     */
    //List pagedList(MangoDetachedCriteria criteria, Pager pager);

}
