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
    def getHibernateCriteriaOrQuery(Object gormQuery) {
        if (gormQuery instanceof AbstractHibernateQuery) {
            ((AbstractHibernateQuery) gormQuery)
            Criteria criteria = (Criteria) getPrivateFieldValue(AbstractHibernateQuery, "criteria", gormQuery)
            Validate.notNull(criteria)
            return criteria
        } else if (gormQuery instanceof HibernateHqlQuery) {
            Query hqlQuery = (Query) getPrivateFieldValue(HibernateHqlQuery, "query", gormQuery)
            Validate.notNull(hqlQuery)
            hqlQuery.getMaxResults()
            return hqlQuery
        }
    }

    /**
     * It would set query timeout on underlying hibernate criteria or hql query
     - `AbstractHibernateQuery` is used by
        - Dynamic finders, "find by example" queries, such as find, findAll, & criteria queries
        - HibernateHqlQuery is used by
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
        int existingMax = hQuery.getMaxResults() ?: max
        max = Math.min(existingMax, max)
        hQuery.setMaxResults(max)
    }
}
