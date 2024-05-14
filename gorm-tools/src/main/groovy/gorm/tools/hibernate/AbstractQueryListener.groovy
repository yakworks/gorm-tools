/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.orm.hibernate.query.AbstractHibernateQuery
import org.grails.orm.hibernate.query.HibernateHqlQuery
import org.hibernate.Criteria
import org.hibernate.query.Query

import yakworks.commons.lang.Validate

import static yakworks.util.ReflectionUtils.getPrivateFieldValue

@CompileStatic
abstract class AbstractQueryListener {

    //returns criteria or hql query based on type of query
    @CompileDynamic
    Object getHibernateCriteriaOrQuery(Object gormQuery) {
        if (gormQuery instanceof AbstractHibernateQuery) {
            ((AbstractHibernateQuery) gormQuery)
            return (Criteria) gormQuery.getHibernateCriteria()
        } else if (gormQuery instanceof HibernateHqlQuery) {
            //Need to get private field value with reflection.
            //Because few of the gorm queries, such as count() uses inner class for HibernateHqlQuery
            //and direct field access with .@ would fail for inner class.
            //See ReflectionUtils tests for example.
            return (Query) getPrivateFieldValue(HibernateHqlQuery, "query", gormQuery)
        }
    }

    /**
     * It would set query timeout on underlying hibernate criteria or hql query
     - `AbstractHibernateQuery` is used by
        - Dynamic finders, "find by example" queries, such as find, findAll, & criteria queries
     - HibernateHqlQuery is used by
        - executeUpdate, get, list, count, exists, and find, findAll queries which takes hql string

     See AbstractHibernateGormStaticApi, HibernateStaticApi & DynamicFinder classes for more details.
     Note: hibernate would set this timeout on underlying jdbc statement.
     however, if timeout is set at hibernate transaction level, hibernate overrides whatever timeout is set on query level.

     * @param query query instance
     * @param timeout timeout in seconds
     */
    @CompileDynamic
    void setTimeout(Object query, int timeout) {
        def hQuery = getHibernateCriteriaOrQuery(query)
        Validate.notNull(hQuery)
        hQuery.setTimeout(timeout)
    }

    @CompileDynamic
    void setMax(Object query, int max) {
        def hQuery = getHibernateCriteriaOrQuery(query)
        Validate.notNull(hQuery)

        //if existing max value on query is smaller, thn dont force higher value configured in config.
        //for example: when page size is set to 10, the max would already be set as 10 and we dont want to foce higher value here.
        int existingMax = hQuery.getMaxResults() ?: max
        max = Math.min(existingMax, max)
        hQuery.setMaxResults(max)
    }
}
