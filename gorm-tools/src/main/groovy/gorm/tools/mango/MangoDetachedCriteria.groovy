/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.gorm.finders.DynamicFinder
import org.grails.datastore.gorm.query.criteria.AbstractDetachedCriteria
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.api.QueryArgumentsAware
import org.grails.datastore.mapping.query.api.QueryableCriteria
import org.grails.orm.hibernate.AbstractHibernateSession

import gorm.tools.mango.hibernate.HibernateMangoQuery
import gorm.tools.mango.jpql.JpqlQueryBuilder
import gorm.tools.mango.jpql.JpqlQueryInfo
import gorm.tools.mango.jpql.SimplePagedQuery
import grails.compiler.GrailsCompileStatic
import grails.gorm.DetachedCriteria
import grails.gorm.PagedResultList
import yakworks.commons.lang.NameUtils

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
@Slf4j
@SuppressWarnings(['MethodCount', 'ClassSize']) //ok for this
@GrailsCompileStatic
class MangoDetachedCriteria<T> extends DetachedCriteria<T> {

    Map<String, String> propertyAliases = [:]
    //auto system created aliases, tracked so we can remove the _sum, _avg, etc.. suffixes in the result transformer
    List<String> systemAliases = [] as List<String>

    /**
     * Constructs a DetachedCriteria instance target the given class and alias for the name
     * The default is to use the short domain name with "_" appended to it.
     * @param targetClass The target class
     * @param alias The root alias to be used in queries
     */
    MangoDetachedCriteria(Class<T> targetClass, String alias = null) {
        super(targetClass, alias)
        if(!alias) this.@alias = "${NameUtils.getPropertyName(targetClass.simpleName)}_"
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
        (T)withQueryInstance(args, additionalCriteria) { Query query ->
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
        (List)withQueryInstance(args, additionalCriteria) { Query query ->
            if(log.debugEnabled){
                log.debug("Query criteria: ${query.criteria}")
            }
            if (args?.max) {
                return new PagedResultList(query)
            }
            return query.list()
        }
    }

    /**
     * Lists all records matching the criterion contained within this DetachedCriteria instance
     * Uses the JpqlQueryBuilder to build jpql with map projections.
     * Forces the results to be in a map even if its only 1 column like a count.
     *
     * @return A list of matching instances
     */
    List<Map> mapList(Map args = Collections.emptyMap()) {
        def builder = JpqlQueryBuilder.of(this) //.aliasToMap(true)
        JpqlQueryInfo queryInfo = builder.buildSelect()
        def api = currentGormStaticApi()
        //use SimplePagedQuery so it can attach the totalCount
        SimplePagedQuery hq = new SimplePagedQuery(api, this.systemAliases)
        //def list = hq.list(queryInfo.query, queryInfo.paramMap, args)
        def list = hq.list(queryInfo.query, queryInfo.paramMap, args)
        return list
    }

    /**
     * Where method derives a new query from this query. This method will not mutate the original query, but instead return a new one.
     *
     * @param additionalQuery The additional query
     * @return A new query
     */
    @Override
    MangoDetachedCriteria<T> where(@DelegatesTo(DetachedCriteria) Closure additionalQuery) {
        MangoDetachedCriteria<T> newQuery = (MangoDetachedCriteria<T>)clone()
        return newQuery.build(additionalQuery)
    }

    /**
     * Where method derives a new query from this query. This method will not mutate the original query, but instead return a new one.
     *
     * @param additionalQuery The additional query
     * @return A new query
     */
    MangoDetachedCriteria<T> where(Map additionalQuery) {
        MangoDetachedCriteria<T> newQuery = (MangoDetachedCriteria<T>)clone()
        new MangoBuilder().applyMapOrList(newQuery, [name2: 'foo'])
        return newQuery
    }

    /**
     * Counts the number of records returned by the query
     *
     * @param args The arguments
     * @return The count
     */
    @Override
    Number count(Map args = Collections.emptyMap(), @DelegatesTo(DetachedCriteria) Closure additionalCriteria = null) {
        (Number)withQueryInstance(args, additionalCriteria) { Query query ->
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
        (Number)withQueryInstance(Collections.emptyMap(), additionalCriteria) { Query query ->
            query.projections().count()
            query.singleResult()
        }
    }

    /**
     * force an error since this creates unpredictiable truthy checks for crieria
     */
    @Override
    boolean asBoolean(@DelegatesTo(DetachedCriteria) Closure additionalCriteria = null) {
        // (Boolean)withQueryInstance(Collections.emptyMap(), additionalCriteria) { Query query ->
        //     query.projections().count()
        //     ((Number)query.singleResult()) > 0
        // }
        throw new UnsupportedOperationException("Truthy check is not supported, use null check instead")
    }

    /**
     * uses the count to check if its greater than 0.
     */
    boolean exists() {
        return (Boolean)withQueryInstance(Collections.emptyMap(), null) { Query query ->
            query.projections().count()
            query.maxResults(1)
            query.projections().id()
            ((Number)query.list().size()) > 0
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
        property = parseAlias(property, "SUM")
        ensureAliases(property)
        projectionList.sum(property)
        return this
    }

    /**
     * Adds a avg projection
     * @param property The property to sum by
     * @return This criteria instance
     */
    @Override
    MangoDetachedCriteria<T> avg(String property) {
        property = parseAlias(property, "AVG")
        ensureAliases(property)
        projectionList.avg(property)
        return this
    }

    @Override
    MangoDetachedCriteria<T> min(String property) {
        property = parseAlias(property, "MIN")
        ensureAliases(property)
        projectionList.min(property)
        return this
    }

    @Override
    MangoDetachedCriteria<T> max(String property) {
        property = parseAlias(property, "MAX")
        ensureAliases(property)
        projectionList.max(property)
        return this
    }

    /**
     * Adds a groupBy projection
     *
     * @param property The property to sum by
     * @return This criteria instance
     */
    MangoDetachedCriteria<T> groupBy(String property) {
        property = parseAlias(property, "")
        ensureAliases(property)
        projectionList.groupProperty(property)
        return this
    }

    /**
     * Adds a countDistinct projection
     *
     * @param property The property to sum by
     * @return This criteria instance
     */
    MangoDetachedCriteria<T> countDistinct(String property) {
        property = parseAlias(property, "COUNT")
        ensureAliases(property)
        projectionList.countDistinct(property)
        return this
    }

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

    /**
     * allows to do a nested association without compile dynamic
     *
     * @param assoc the nested association
     * @param args the closiure to call on it
     * @return the object reply of the super.invokeMethod
     */
    @CompileDynamic
    def assoc(String assoc, Closure args) {
        super.invokeMethod(assoc, args)
    }

    @Override
    protected MangoDetachedCriteria<T> clone() {
        AbstractDetachedCriteria criteria = (MangoDetachedCriteria)super.clone()
        criteria.propertyAliases = propertyAliases
        return criteria
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

    /**
     * Overrides the private withPopulatedQuery in super.
     * creates a HibernateMangoQuery instance and pass it to the closure
     */
    def withQueryInstance(Map args, Closure additionalCriteria, Closure closure)  {
        Query query = createQueryInstance(args, additionalCriteria)
        closure.call(query)
    }

    HibernateMangoQuery getHibernateQuery() {
        createQueryInstance([:], null) as HibernateMangoQuery
    }

    GormStaticApi<T> currentGormStaticApi() {
        (GormStaticApi<T>) (persistentEntity.isMultiTenant() ?
            GormEnhancer.findStaticApi(targetClass) : GormEnhancer.findStaticApi(targetClass, connectionName))
    }

    /**
     * moved in from  private super.withPopulatedQuery.
     * Creates a HibernateMangoQuery or in testing falls back to the session.createQuery
     */
    Query createQueryInstance(Map args, Closure additionalCriteria) {
        Query query

        GormStaticApi staticApi = currentGormStaticApi()

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
    // static void applyDetachedCriteria(Query query, AbstractDetachedCriteria detachedCriteria) {
    //     if (detachedCriteria != null) {
    //         Map<String, FetchType> fetchStrategies = detachedCriteria.getFetchStrategies();
    //         for (Map.Entry<String, FetchType> entry : fetchStrategies.entrySet()) {
    //             String property = entry.getKey();
    //             switch(entry.getValue()) {
    //                 case FetchType.EAGER:
    //                     JoinType joinType = (JoinType) detachedCriteria.getJoinTypes().get(property);
    //                     if(joinType != null) {
    //                         query.join(property, joinType);
    //                     }
    //                     else {
    //                         query.join(property);
    //                     }
    //                     break;
    //                 case FetchType.LAZY:
    //                     query.select(property);
    //             }
    //         }
    //         List<Query.Criterion> criteria = detachedCriteria.getCriteria();
    //         for (Query.Criterion criterion : criteria) {
    //             query.add(criterion);
    //         }
    //         List<Query.Projection> projections = detachedCriteria.getProjections();
    //         for (Query.Projection projection : projections) {
    //             query.projections().add(projection);
    //         }
    //         List<Query.Order> orders = detachedCriteria.getOrders();
    //         for (Query.Order order : orders) {
    //             query.order(order);
    //         }
    //     }
    // }

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
     * For props with dots in them, for example foo.bar.baz. Will ensure the nested aliases are setup for foo and foo.bar
     * also checks to see if prop is in form "name as alias" for example "foo.bar.baz as baz" so it can track and setup the property
     * alias if its sent to JpqlQueryBuilder and its an aliasToMap
     * @return the property to use. will be same as whats passed in unless it has " as " will only return first portion
     */
    void ensureAliases(String prop){

        // if (!propertyName.contains('.') || propertyName.endsWith('.id'))
        if(prop.count('.') < 1 || (prop.count('.') == 1 && prop.endsWith('.id'))) return

        List<String> props = prop.split(/\./) as List<String>
        String first = props[0]
        String field = props.removeLast()

        //make sure there are nested criterias for the order
        DetachedCriteria currentCriteria = this as DetachedCriteria
        props.each { path ->
            currentCriteria = currentCriteria.createAlias(path, path) as DetachedCriteria
        }
    }

    String parseAlias(String p, String key) {
        String prop = p.trim()
        String aliasKey = "${key}_${p}"
        if(prop.contains(" as ")) {
            String[] parts = prop.split(/\sas\s/)
            p = parts[0].trim()
            aliasKey = "${key}_${p}"
            propertyAliases[aliasKey] = parts[1].trim()
        } else {
            //if no key its groupby so just return it
            if(!key) return p
            String alas = p.replace('.', '_')
            alas = "${alas}_${key.toLowerCase()}"
            propertyAliases[aliasKey] = alas
            systemAliases << alas
        }
        return p
    }

    /******** PROPERTY CRITERIAS ************/

    @Override
    MangoDetachedCriteria<T> "in"(String propertyName, Collection values) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.in(propertyName, values)
    }

    @Override
    MangoDetachedCriteria<T> "in"(String propertyName, QueryableCriteria subquery) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.in(propertyName, subquery)
    }

    @Override
    MangoDetachedCriteria<T> inList(String propertyName, QueryableCriteria<?> subquery) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.inList(propertyName, subquery)
    }

    @Override
    MangoDetachedCriteria<T> "in"(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> subquery) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.in(propertyName, subquery)
    }

    @Override
    MangoDetachedCriteria<T> inList(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> subquery) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.inList(propertyName, subquery)
    }

    @Override
    MangoDetachedCriteria<T> "in"(String propertyName, Object[] values) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.in(propertyName, values)
    }

    @Override
    MangoDetachedCriteria<T> notIn(String propertyName, QueryableCriteria<?> subquery) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.notIn(propertyName, subquery)
    }

    @Override
    MangoDetachedCriteria<T> notIn(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> subquery) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.notIn(propertyName, subquery)
    }

    // @Override
    // MangoDetachedCriteria<T> inList(String propertyName, Collection values) {
    //     ensureAliases(propertyName)
    //     return (MangoDetachedCriteria<T>)super.inList(propertyName, values)
    // }

    @Override
    MangoDetachedCriteria<T> inList(String propertyName, Object[] values) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.inList(propertyName, values)
    }

    @Override
    MangoDetachedCriteria<T> sizeEq(String propertyName, int size) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.sizeEq(propertyName, size)
    }

    @Override
    MangoDetachedCriteria<T> sizeGt(String propertyName, int size) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.sizeGt(propertyName, size)
    }

    @Override
    MangoDetachedCriteria<T> sizeGe(String propertyName, int size) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.sizeGe(propertyName, size)
    }

    @Override
    MangoDetachedCriteria<T> sizeLe(String propertyName, int size) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.sizeLe(propertyName, size)
    }

    @Override
    MangoDetachedCriteria<T> sizeLt(String propertyName, int size) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.sizeLt(propertyName, size)
    }

    @Override
    MangoDetachedCriteria<T> sizeNe(String propertyName, int size) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.sizeNe(propertyName, size)
    }

    @Override
    MangoDetachedCriteria<T> eqProperty(String propertyName, String otherPropertyName) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.eqProperty(propertyName, otherPropertyName)
    }

    @Override
    MangoDetachedCriteria<T> neProperty(String propertyName, String otherPropertyName) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.neProperty(propertyName, otherPropertyName)
    }

    @Override
    MangoDetachedCriteria<T> allEq(Map<String, Object> propertyValues) {

        return (MangoDetachedCriteria<T>)super.allEq(propertyValues)
    }

    @Override
    MangoDetachedCriteria<T> gtProperty(String propertyName, String otherPropertyName) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.gtProperty(propertyName, otherPropertyName)
    }

    @Override
    MangoDetachedCriteria<T> geProperty(String propertyName, String otherPropertyName) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.geProperty(propertyName, otherPropertyName)
    }

    @Override
    MangoDetachedCriteria<T> ltProperty(String propertyName, String otherPropertyName) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.ltProperty(propertyName, otherPropertyName)
    }

    @Override
    MangoDetachedCriteria<T> leProperty(String propertyName, String otherPropertyName) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.leProperty(propertyName, otherPropertyName)
    }

    @Override
    MangoDetachedCriteria<T> isEmpty(String propertyName) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.isEmpty(propertyName)
    }

    @Override
    MangoDetachedCriteria<T> isNotEmpty(String propertyName) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.isNotEmpty(propertyName)
    }

    @Override
    MangoDetachedCriteria<T> isNull(String propertyName) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.isNull(propertyName)
    }

    @Override
    MangoDetachedCriteria<T> isNotNull(String propertyName) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.isNotNull(propertyName)
    }
    //
    // @Override
    // MangoDetachedCriteria<T> eq(String propertyName, Object propertyValue) {
    //     ensureAliases(propertyName)
    //     return (MangoDetachedCriteria<T>)super.eq(propertyName, propertyValue)
    // }

    @Override
    MangoDetachedCriteria<T> idEq(Object propertyValue) {
        return (MangoDetachedCriteria<T>)super.idEq(propertyValue)
    }

    // @Override
    // MangoDetachedCriteria<T> ne(String propertyName, Object propertyValue) {
    //     ensureAliases(propertyName)
    //     return (MangoDetachedCriteria<T>)super.ne(propertyName, propertyValue)
    // }

    @Override
    MangoDetachedCriteria<T> between(String propertyName, Object start, Object finish) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.between(propertyName, start, finish)
    }

    @Override
    MangoDetachedCriteria<T> gte(String propertyName, Object value) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.gte(propertyName, value)
    }

    @Override
    MangoDetachedCriteria<T> ge(String propertyName, Object value) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.ge(propertyName, value)
    }

    @Override
    MangoDetachedCriteria<T> gt(String propertyName, Object value) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.gt(propertyName, value)
    }

    @Override
    MangoDetachedCriteria<T> lte(String propertyName, Object value) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.lte(propertyName, value)
    }

    @Override
    MangoDetachedCriteria<T> le(String propertyName, Object value) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.le(propertyName, value)
    }

    @Override
    MangoDetachedCriteria<T> lt(String propertyName, Object value) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.lt(propertyName, value)
    }

    @Override
    MangoDetachedCriteria<T> like(String propertyName, Object propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.like(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> ilike(String propertyName, Object propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.ilike(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> rlike(String propertyName, Object propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.rlike(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> eqAll(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.eqAll(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> gtAll(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.gtAll(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> ltAll(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.ltAll(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> geAll(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.geAll(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> leAll(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.leAll(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> eqAll(String propertyName, QueryableCriteria propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.eqAll(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> gtAll(String propertyName, QueryableCriteria propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.gtAll(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> gtSome(String propertyName, QueryableCriteria propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.gtSome(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> gtSome(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.gtSome(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> geSome(String propertyName, QueryableCriteria propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.geSome(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> geSome(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.geSome(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> ltSome(String propertyName, QueryableCriteria propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.ltSome(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> ltSome(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.ltSome(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> leSome(String propertyName, QueryableCriteria propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.leSome(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> leSome(String propertyName, @DelegatesTo(AbstractDetachedCriteria) Closure<?> propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.leSome(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> ltAll(String propertyName, QueryableCriteria propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.ltAll(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> geAll(String propertyName, QueryableCriteria propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.geAll(propertyName, propertyValue)
    }

    @Override
    MangoDetachedCriteria<T> leAll(String propertyName, QueryableCriteria propertyValue) {
        ensureAliases(propertyName)
        return (MangoDetachedCriteria<T>)super.leAll(propertyName, propertyValue)
    }

}
