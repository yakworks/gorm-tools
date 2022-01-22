/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import groovy.transform.CompileDynamic

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.gorm.finders.DynamicFinder
import org.grails.datastore.gorm.query.criteria.AbstractDetachedCriteria
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.QueryArgumentsAware
import org.grails.orm.hibernate.AbstractHibernateSession
import org.grails.orm.hibernate.query.HibernateQuery

import grails.compiler.GrailsCompileStatic
import grails.gorm.DetachedCriteria
import grails.gorm.PagedResultList

import javax.persistence.FetchType
import javax.persistence.criteria.JoinType

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
@GrailsCompileStatic
class MangoDetachedCriteria<T> extends DetachedCriteria<T> {


    /**
     * Constructs a DetachedCriteria instance target the given class and alias for the name
     * @param targetClass The target class
     * @param alias The root alias to be used in queries
     */
    MangoDetachedCriteria(Class<T> targetClass, String alias = null) {
        super(targetClass, alias)
    }

    @Override
    protected MangoDetachedCriteria newInstance() {
        new MangoDetachedCriteria(targetClass, alias)
    }

    /**
     * Returns a single result matching the criterion contained within this DetachedCriteria instance
     *
     * @return A single entity
     */
    @Override
    T get(Map args = Collections.emptyMap(), @DelegatesTo(DetachedCriteria) Closure additionalCriteria = null) {
        (T)withPopulatedQuery(args, additionalCriteria) { Query query ->
            query.singleResult()
        }
    }

    /**
     * Lists all records matching the criterion contained within this DetachedCriteria instance
     *
     * @return A list of matching instances
     */
    @Override
    List<T> list(Map args = Collections.emptyMap(), @DelegatesTo(DetachedCriteria) Closure additionalCriteria = null) {
        (List)withPopulatedQuery(args, additionalCriteria) { Query query ->
            if (args?.max) {
                return new PagedResultList(query)
            }
            return query.list()
        }
    }

    /**
     * Lists all records matching the criterion contained within this DetachedCriteria instance
     *
     * @return A list of matching instances
     */
    List<Map> mapList() {
        getHibernateQuery().mapList() as List<Map>
    }

    /**
     * Counts the number of records returned by the query
     *
     * @param args The arguments
     * @return The count
     */
    @Override
    Number count(Map args = Collections.emptyMap(), @DelegatesTo(DetachedCriteria) Closure additionalCriteria = null) {
        (Number)withPopulatedQuery(args, additionalCriteria) { Query query ->
            query.projections().count()
            query.singleResult()
        }
    }

    /**
     * Counts the number of records returned by the query
     *
     * @param args The arguments
     * @return The count
     */
    @Override
    Number count(@DelegatesTo(DetachedCriteria) Closure additionalCriteria) {
        (Number)withPopulatedQuery(Collections.emptyMap(), additionalCriteria) { Query query ->
            query.projections().count()
            query.singleResult()
        }
    }

    /**
     * Counts the number of records returned by the query
     *
     * @param args The arguments
     * @return The count
     */
    @Override
    boolean asBoolean(@DelegatesTo(DetachedCriteria) Closure additionalCriteria = null) {
        (Boolean)withPopulatedQuery(Collections.emptyMap(), additionalCriteria) { Query query ->
            query.projections().count()
            ((Number)query.singleResult()) > 0
        }
    }

    /**
     * Adds a sum projection
     *
     * @param property The property to sum by
     * @return This criteria instance
     */
    @Override
    MangoDetachedCriteria<T> sum(String property) {
        ensureAliases(property)
        projectionList.sum(property)
        return this
    }

    /**
     * Adds a groupBy projection
     *
     * @param property The property to sum by
     * @return This criteria instance
     */
    MangoDetachedCriteria<T> groupBy(String property) {
        ensureAliases(property)
        projectionList.groupProperty(property)
        return this
    }

    /**
     * Adds a groupBy projection
     *
     * @param property The property to sum by
     * @return This criteria instance
     */
    MangoDetachedCriteria<T> countDistinct(String property) {
        ensureAliases(property)
        projectionList.countDistinct(property)
        return this
    }

    /**
     * Orders by the specified property name (defaults to ascending)
     *
     * @param propertyName The property name to order by
     * @return A Order instance
     */
    // @Override
    // MangoDetachedCriteria<T> order(String propertyName) {
    //     return order(propertyName, "asc")
    // }

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
    // @CompileDynamic
    // MangoDetachedCriteria<T> order(String propertyName, String direction, boolean forceSuper = false) {
    //     if (forceSuper || !propertyName.contains('.')) {
    //         return super.order(propertyName, direction)
    //     }
    //     List props = propertyName.split(/\./) as List
    //     String last = props.pop()
    //     Closure toDo = { order(last, direction) }
    //     Closure newOrderBy = props.reverse().inject(toDo) { acc, prop ->
    //         { -> "$prop"(acc) }
    //     }
    //     newOrderBy.call()
    //     return this
    // }

    @Override
    MangoDetachedCriteria<T> eq(String propertyName, Object propertyValue) {
        nestedPathPropCall(propertyName, propertyValue, "eq")
    }

    @Override
    MangoDetachedCriteria<T> ne(String propertyName, Object propertyValue) {
        nestedPathPropCall(propertyName, propertyValue, "ne")
    }

    @Override
    MangoDetachedCriteria<T> inList(String propertyName, Collection values) {
        nestedPathPropCall(propertyName, values, "inList")
    }

    @CompileDynamic
    MangoDetachedCriteria<T> nestedPathPropCall(String propertyName, Object propertyValue, String critName) {
        if (!propertyName.contains('.') || propertyName.endsWith('.id')) {
            return super."$critName"(propertyName, propertyValue)
        }
        List props = propertyName.split(/\./) as List
        String last = props.removeLast()
        Closure toDo = { "$critName"(last, propertyValue) }
        Closure newCall = props.reverse().inject(toDo) { Closure acc, String prop ->
            { -> "$prop"(acc) }
        }
        newCall.call()
        return this
    }

    @CompileDynamic
    def assoc(String assoc, Closure args) {
        super.invokeMethod(assoc, args)
    }

    @Override
    protected MangoDetachedCriteria<T> clone() {
        return (MangoDetachedCriteria)super.clone()
    }

    /**
     * Enable the builder syntax for constructing Criteria
     *
     * @param callable The callable closure
     * @return A new criteria instance
     */
    @Override
    MangoDetachedCriteria<T> build(@DelegatesTo(MangoDetachedCriteria) Closure callable) {
        (MangoDetachedCriteria<T>)super.build(callable)
    }

    def withPopulatedQuery(Map args, Closure additionalCriteria, Closure callable)  {
        Query query = getQueryInstance(args, additionalCriteria)
        callable.call(query)
    }

    HibernateMangoQuery getHibernateQuery() {
        getQueryInstance([:], null) as HibernateMangoQuery
    }

    Query getQueryInstance(Map args, Closure additionalCriteria) {
        Query query

        GormStaticApi staticApi = persistentEntity.isMultiTenant() ?
            GormEnhancer.findStaticApi(targetClass) : GormEnhancer.findStaticApi(targetClass, connectionName)

        staticApi.withDatastoreSession { Session session ->
            applyLazyCriteria()
            if(session instanceof AbstractHibernateSession) {
                // query = session.createQuery(targetClass, alias)
                query = HibernateMangoQuery.createQuery( (AbstractHibernateSession)session, persistentEntity, alias)
            }
            else {
                query = session.createQuery(targetClass)
            }


            if (defaultMax != null) {
                query.max(defaultMax)
            }
            if (defaultOffset != null) {
                query.offset(defaultOffset)
            }
            DynamicFinder.applyDetachedCriteria(query, this)
            //applyDetachedCriteria(query, this)

            if(query instanceof QueryArgumentsAware) {
                ((QueryArgumentsAware)query).arguments = args
            }

            if (additionalCriteria != null) {
                def additionalDetached = new DetachedCriteria(targetClass).build(additionalCriteria)
                DynamicFinder.applyDetachedCriteria(query, additionalDetached)
            }

            DynamicFinder.populateArgumentsForCriteria(targetClass, query, args)
        }

        return query
    }

    // copied in from DynamicFinder.applyDetachedCriteria
    static void applyDetachedCriteria(Query query, AbstractDetachedCriteria detachedCriteria) {
        if (detachedCriteria != null) {
            Map<String, FetchType> fetchStrategies = detachedCriteria.getFetchStrategies();
            for (Map.Entry<String, FetchType> entry : fetchStrategies.entrySet()) {
                String property = entry.getKey();
                switch(entry.getValue()) {
                    case FetchType.EAGER:
                        JoinType joinType = (JoinType) detachedCriteria.getJoinTypes().get(property);
                        if(joinType != null) {
                            query.join(property, joinType);
                        }
                        else {
                            query.join(property);
                        }
                        break;
                    case FetchType.LAZY:
                        query.select(property);
                }
            }
            List<Query.Criterion> criteria = detachedCriteria.getCriteria();
            for (Query.Criterion criterion : criteria) {
                query.add(criterion);
            }
            List<Query.Projection> projections = detachedCriteria.getProjections();
            for (Query.Projection projection : projections) {
                query.projections().add(projection);
            }
            List<Query.Order> orders = detachedCriteria.getOrders();
            for (Query.Order order : orders) {
                query.order(order);
            }
        }
    }

    @Override
    MangoDetachedCriteria<T> order(String propertyName) {
        return order(new Query.Order(propertyName))
    }

    @Override
    MangoDetachedCriteria<T> order(String propertyName, String direction) {
        return order(new Query.Order(propertyName, Query.Order.Direction.valueOf(direction.toUpperCase())))
    }

    @Override
    MangoDetachedCriteria<T> order(Query.Order o) {
        ensureAliases(o.property)
        orders << o
        return this
    }

    /**
     * For props with dots in them, for example foo.bar.baz. Will ensure the nested aliases are setup
     * for foo and foo.bar
     */
    void ensureAliases(String prop){
        if(prop.count('.') < 1) return

        List<String> props = prop.split(/\./) as List<String>
        String first = props[0]
        String field = props.removeLast()

        //make sure there are nested criterias for the order
        DetachedCriteria currentCriteria = this as DetachedCriteria
        props.each { path ->
            currentCriteria = currentCriteria.createAlias(path, path) as DetachedCriteria
        }
    }

}
