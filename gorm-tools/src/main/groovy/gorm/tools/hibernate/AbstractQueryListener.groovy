/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.query.Query
import org.grails.orm.hibernate.query.HibernateHqlQuery
import org.grails.orm.hibernate.query.HibernateQuery
import org.hibernate.Criteria

import gorm.tools.mango.hibernate.HibernateMangoQuery

import static yakworks.util.ReflectionUtils.getPrivateFieldValue

@CompileStatic
abstract class AbstractQueryListener {

    /**
     * gets the Gorm criteria or hibernate Query, depends on what kind of gormQuery this event is for.
     * @param gormQuery the gorm Query that was set in the Event.
     * @return either the Criteria or the org.hibernate.query.Query
     */
    Object getHibernateCriteriaOrQuery(Query gormQuery) {
        if (gormQuery instanceof HibernateMangoQuery) {
            return gormQuery.getHibernateCriteria()
        } else if (gormQuery instanceof HibernateQuery) {
            return gormQuery.getHibernateCriteria()
        } else if (gormQuery instanceof HibernateHqlQuery) {
            // Need to get private field value with reflection. Using groovy .@ doesnt work here because of inner classes
            // See ReflectionUtils tests for example.
            return (org.hibernate.query.Query) getPrivateFieldValue(HibernateHqlQuery, "query", gormQuery)
        }
    }

    /**
     * It would set query timeout on underlying hibernate criteria or hql query
     - `AbstractHibernateQuery` is used by
        - Dynamic finders, "find by example" queries, such as find, findAll, & criteria queries
     - HibernateHqlQuery is used by
        - executeUpdate, get, list, count, exists, and find, findAll queries which takes hql string
     //XXX is above true when its not multiTenant? I dont see it firing on get or list. I set breakpoint and ran some unit tests
     // that should have fired.

     See AbstractHibernateGormStaticApi, HibernateStaticApi & DynamicFinder classes for more details.
     Note: hibernate would set this timeout on underlying jdbc statement.
     however, if timeout is set at hibernate transaction level, hibernate overrides whatever timeout is set on query level.

     * @param query query instance
     * @param timeout timeout in seconds
     */
    void setTimeout(Query query, int timeout) {
        var hQuery = getHibernateCriteriaOrQuery(query)
        assert hQuery //should never be null
        if (hQuery instanceof Criteria) {
            hQuery.setTimeout(timeout)
        } else if (hQuery instanceof org.hibernate.query.Query) {
            hQuery.setTimeout(timeout)
        }
    }

    // @CompileDynamic
    // void setMax(Query query, int max) {
    //     def hQuery = getHibernateCriteriaOrQuery(query)
    //     Validate.notNull(hQuery)
    //
    //     //if existing max value on query is smaller, thn dont force higher value configured in config.
    //     //for example: when page size is set to 10, the max would already be set as 10 and we dont want to foce higher value here.
    //     int existingMax = hQuery.getMaxResults() ?: max
    //     max = Math.min(existingMax, max)
    //     hQuery.setMaxResults(max)
    // }
}
