/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import gorm.tools.beans.Pager
import gorm.tools.mango.api.MangoQuery
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional

import static gorm.tools.mango.MangoOps.CRITERIA

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

    @Value('${gorm.tools.mango.criteriaKeyName:criteria}')
    //gets criteria keyword from config, if there is no, then uses 'criteria'
    String criteriaKeyName

    /**
     * Builds detached criteria for repository's domain based on mango criteria language and additional criteria
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    public <D> MangoDetachedCriteria<D> query(Class<D> domainClass, Map criteria = [:],
                                              @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {
        mangoBuilder.build(domainClass, criteria, closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    public <D> List<D> queryList(Class<D> domainClass, Map params = [:], @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {
        Map<String, Map> p = parseParams(params)
        DetachedCriteria<D> dcrit = query(domainClass, p['criteria'], closure)
        Pager pager = new Pager(p['pager'])
        list(dcrit, pager)
    }

    /**
     * call list on the criteria with the pager params inside a readOnly transaction
     *
     * @param criteria the built detached criteria
     * @param pagerParams the map with max, offset and page
     * @return list of entities
     */
    @Transactional(readOnly = true)
    public <D> List<D> list(DetachedCriteria<D> criteria, Pager pager) {
        criteria.list(max: pager.max, offset: pager.offset)
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

    /**
     * returns a Map with a criteria key and a pager key containing maps for those.
     */
    Map<String, Map> parseParams(Map<String, ?> params){
        Map pager = [:]
        //def result = [criteria: [:], pager: [:]] as Map<String, Map>

        Map criteria = [:]
        criteria.putAll(params)
        //pull out the max, page and offset and assume the rest is criteria
        Map pagerMap = [:]
        ['max', 'offset', 'page'].each{ String k ->
            if(criteria.containsKey(k)) pagerMap[k] = criteria.remove(k)
        }
        if(pagerMap) pager = pagerMap

        //pull out the sort if its there
        //clean up sort
        def sort
        if (criteria['sort']) {
            sort = criteria.remove('sort')
            if(criteria['order']) {
                Map newSort = [:]
                newSort[sort] = criteria.remove('order')
                sort = newSort
            }
        }

        // check for q param or citeria, if it has it then it overrides whats in the map
        def qCriteria = criteria.q ? criteria.remove('q') : criteria.remove(CRITERIA)
        String qString

        if(qCriteria) {
            if (qCriteria instanceof String) {
                qString = qCriteria as String
                if (qString.trim().startsWith('{')) {
                    criteria = new JsonSlurper().parseText(qString) as Map
                    // JSON.use('deep')
                    // result['criteria'] = JSON.parse(params[criteriaKeyName] as String) as Map
                }
                else if(params.containsKey('qSearchFields')) { //if it has a qsFields then set up the map
                    Map qMap = ['text': qString, 'fields': criteria.remove('qSearchFields')]
                    criteria['$q'] = qMap
                }
            } else {
                criteria = qCriteria as Map
            }
        }
        // if sort is populated
        if(sort) criteria['$sort'] = sort

        return [criteria: criteria, pager: pager]
    }
}
