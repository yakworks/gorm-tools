/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.criteria

import groovy.transform.CompileDynamic

import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.Criteria
import org.hibernate.SessionFactory

import grails.compiler.GrailsCompileStatic
import grails.orm.HibernateCriteriaBuilder

/**
 * This is here to make it easier to build criteria with domain bean paths
 * allows
 * order('invoice.customer.name')
 *
 * instead of
 * invoice {
 *    customer {
 *       order(name)
 *    }
 * }
 *
 * simliar with eq, like and in
 *
 * ilike('invoice.customer.name', 'foo')
 */
@Deprecated
@GrailsCompileStatic
class GormHibernateCriteriaBuilder extends HibernateCriteriaBuilder {

    GormHibernateCriteriaBuilder(Class arg1, SessionFactory arg2) {
        super(arg1, arg2)
    }

    /**
     * Orders by the specified property name (defaults to ascending)
     *
     * @param propertyName The property name to order by
     * @return A Order instance
     */
    Criteria order(String propertyName) {
        return order(propertyName, "asc")
    }

    /**
     * Orders by the specified property name and direction
     * takes invoice.customer.name and builds a closure that looks like
     *
     * invoice {
     *    customer {
     *       order(name)
     *    }
     * }
     * and then calls the that closure on this.
     *
     * @param propertyName The property name to order by
     * @param direction Either "asc" for ascending or "desc" for descending
     * @param forceSuper use original order(...) from HibernateCriteriaBuilder
     *
     * @return A Order instance
     */
    @CompileDynamic
    Criteria order(String propertyName, String direction, boolean forceSuper = false) {
        if (forceSuper || !propertyName.contains('.')) {
            return super.order(propertyName, direction)
        }
        List props = propertyName.split(/\./) as List
        String last = props.removeLast()
        Closure toDo = { order(last, direction) }
        Closure newOrderBy = props.reverse().inject(toDo) { acc, prop ->
            { -> "$prop"(acc) }
        }
        newOrderBy.call()
        return this
    }

    /**
     * Creates a Criterion with from the specified property name and "like" expression
     * @param propertyName The property name
     * @param propertyValue The like value
     *
     * @return A Criterion instance
     */
    Criteria like(String propertyName, Object propertyValue) {
        nestedPathPropCall(propertyName, propertyValue, "like")
    }

    Criteria ilike(String propertyName, Object propertyValue) {
        nestedPathPropCall(propertyName, propertyValue, "ilike")
    }

    Criteria eq(String propertyName, Object propertyValue) {
        nestedPathPropCall(propertyName, propertyValue, "eq")
    }

    Criteria inList(String propertyName, Collection values) {
        nestedPathPropCall(propertyName, values, "in")
    }

    Criteria inList(String propertyName, Object[] values) {
        nestedPathPropCall(propertyName, values, "in")
    }

    @CompileDynamic
    Criteria nestedPathPropCall(String propertyName, Object propertyValue, String critName) {
        if (!propertyName.contains('.')) {
            return super."$critName"(propertyName, propertyValue)
        }
        List props = propertyName.split(/\./) as List
        String last = props.removeLast()
        Closure toDo = { "$critName"(last, propertyValue) }
        Closure newCall = props.reverse().inject(toDo) { acc, prop ->
            { -> "$prop"(acc) }
        }
        newCall.call()
        return this
    }

    /**
     * Dynamic method dispatch fail!
     */
    Object methodMissing(String name, Object args) {
        return super.invokeMethod(name, args)
    }

}

/**
 * This class exists solely to circumvent the "protected" visibility of the org.hibernate.criterion.Order class constructor.
 */
@Deprecated
@CompileDynamic
class OrderCheater extends Query.Order {
    OrderCheater(String propertyName, boolean ascending) {
        super(propertyName, ascending)
    }
}
