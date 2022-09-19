/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.beans.Pager
import gorm.tools.mango.api.MangoQuery
import gorm.tools.mango.api.QueryArgs
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional

/**
 * Default implementation of MangoQuery. Setup as spring bean that is used by all the repos
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class DefaultMangoQuery implements MangoQuery {

    @Autowired
    MangoBuilder mangoBuilder

    JsonSlurper jsonSlurper = new JsonSlurper().setType(JsonParserType.LAX)

    @Override
    public <D> MangoDetachedCriteria<D> query(Class<D> entityClass, Map params,
                                              @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {
        return query(entityClass, QueryArgs.of(params), closure)
    }

    /**
     *  Builds detached criteria for repository's domain based on mango criteria language
     *
     * @param entityClass the base entity class
     * @param qargs the QueryArgs with the prepared criteria in it.
     * @param closure extra criterai closure
     * @return the detached criteria to call list or get on
     */
    public <D> MangoDetachedCriteria<D> query(Class<D> entityClass, QueryArgs qargs,
                                              @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {
        mangoBuilder.buildWithQueryArgs(entityClass, qargs, closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    // public <D> List<D> queryList(Class<D> domainClass, Map params, @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {
    //     return queryList(domainClass, QueryArgs.of(params), closure)
    // }

    // public <D> List<D> queryList(Class<D> domainClass, QueryArgs qargs, @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {
    //     DetachedCriteria<D> dcrit = query(domainClass, qargs, closure)
    //     list(dcrit, qargs.pager)
    // }

    /**
     * call list on the criteria with the pager params inside a readOnly transaction.
     * If it has projections then it will use the JpqlQueryBuilder so that there is more flexibility in
     * using the having clause.
     *
     * @param criteria the built detached criteria
     * @param pagerParams the map with max, offset and page
     * @return list of entities
     */
    @Transactional(readOnly = true)
    public <D> List<D> list(MangoDetachedCriteria<D> criteria, Pager pager) {
        return mangoBuilder.list(criteria, [max: pager.max, offset: pager.offset]) as List<D>
    }

    /**
     *  Calculates sums for specified properties in enities query restricted by mango criteria
     *
     * @param params mango language criteria map
     * @param sums query of properties names that sums should be calculated for
     * @param closure additional restriction for criteria
     * @return map where keys are names of fields and value - sum for restricted entities
     */
    @Transactional(readOnly = true)
    Map countTotals(Class domainClass, Map params = [:], List<String> sums, @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {

        DetachedCriteria mangoCriteria = query(domainClass, params, closure)

        List totalList
        totalList = mangoCriteria.list {
            projections {
                for (String sumField : sums) {
                    sum(sumField)
                }
            }
        }

        List totalsData = (List) totalList[0]
        Map result = [:]
        sums.eachWithIndex { String name, Integer i ->
            result[name] = totalsData[i]
        }
        return result
    }

}
