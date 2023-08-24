/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.jpql

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormStaticApi
import org.grails.orm.hibernate.GrailsHibernateTemplate
import org.grails.orm.hibernate.HibernateGormStaticApi
import org.grails.orm.hibernate.query.HibernateHqlQuery
import org.hibernate.ScrollableResults
import org.hibernate.Session
import org.hibernate.query.Query

import gorm.tools.mango.hibernate.PathKeyMapPagedList
import gorm.tools.mango.hibernate.PathKeyMapResultTransformer

/**
 * Helper to get the hibernate template and query so we can do scrollable to add totalCount logic with JPAQL.
 * Primarily for our projection and aggregate queries, especially with having clauses, are not easy to simply add count substition.
 *
 * TODO If performance will be an issue with Scrollable then alternaitive is to wrap the query as and do a select count(*) on it
 * But that will be tricky with HQL.
 */
@CompileStatic
class PagedQuery {
    // org.hibernate.query.Query query
    HibernateGormStaticApi staticApi
    GrailsHibernateTemplate hibernateTemplate

    /**
     * The aliases we created.
     * for example a projection:['amount':'sum'] will get an alias of 'amount_sum',
     * we remove that _sum suffix automatically because its set in the systemAliases.
     * but if we did projection:['amount as amount_totals':'sum'] then that is user specified. we dont want to remove the _totals suffix.
     */
    List<String> systemAliases = [] as List<String>

    PagedQuery(GormStaticApi staticApi) {
        this.staticApi = (HibernateGormStaticApi)staticApi;
        hibernateTemplate = this.staticApi.getHibernateTemplate()
    }

    PagedQuery(GormStaticApi staticApi, List<String> systemAliases) {
        this(staticApi)
        this.systemAliases = systemAliases
    }


    Integer countQuery(CharSequence queryString, Map params){

        def template = hibernateTemplate
        params = new HashMap(params)

        if(queryString instanceof GString) {
            queryString = buildNamedParameterQueryFromGString((GString) queryString, params)
        }

        return (Integer) template.execute { Session session ->
            Query q = (Query) session.createQuery(queryString.toString())
            populateQueryWithNamedArguments(q, params)
            q.setReadOnly(true)
            // MIN_VALUE gives hint to JDBC driver to stream results
            q.setFetchSize(50) // double normal paging size
            ScrollableResults results = q.scroll()
            results.last()
            int total = results.getRowNumber() + 1
            results.close()
            return total
        }
    }

    /**
     * Executes and returns a PagedList for a JPAQL Query
     */
    public PathKeyMapPagedList list(CharSequence queryString, Map params, Map args) {
        def template = hibernateTemplate
        args = new HashMap(args)
        Integer maxCount = args.max as Integer
        params = new HashMap(params)

        if(queryString instanceof GString) {
            queryString = buildNamedParameterQueryFromGString((GString) queryString, params)
        }

        List dataList = template.execute { Session session ->
            Query q = (Query) session.createQuery(queryString.toString())
            template.applySettings(q)

            populateQueryArguments(q, params)
            populateQueryArguments(q, args)
            populateQueryWithNamedArguments(q, params)
            //sets the transformer to remove the _sum, _avg, etc.. systemAliases
            //q.setResultTransformer(new AliasProjectionResultTransformer(systemAliases))
            q.setResultTransformer(new PathKeyMapResultTransformer(systemAliases))

            def qry = createHqlQuery(session, q)
            def list = qry.list()
            return list
        }
        //only do the count query if its needed
        int rowCount = dataList.size()
        if(rowCount > 1 && maxCount && rowCount >= maxCount){
            rowCount = countQuery(queryString, params)
        }
        PathKeyMapPagedList pagedList = new PathKeyMapPagedList(dataList, rowCount)
        return pagedList
    }

    @CompileDynamic //get around the protected
    HibernateHqlQuery createHqlQuery(Session session, Query q)  {
        return staticApi.createHqlQuery(session, q)
    }

    @CompileDynamic //get around the protected
    void populateQueryArguments(Query q, Map args)  {
        staticApi.populateQueryArguments(q, args)
    }

    @CompileDynamic //get around the protected
    void populateQueryWithNamedArguments(Query q, Map queryNamedArgs)  {
        staticApi.populateQueryWithNamedArguments(q, queryNamedArgs)
    }

    @CompileDynamic //get around the protected
    String buildNamedParameterQueryFromGString(GString query, Map params) {
        return staticApi.buildNamedParameterQueryFromGString(query, params)
    }

}
