/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import java.time.format.DateTimeParseException

import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.InvokerInvocationException
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.beans.Pager
import gorm.tools.mango.api.QueryArgs
import gorm.tools.mango.api.QueryService
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import yakworks.api.problem.data.DataProblem
import yakworks.spring.AppCtx

/**
 * Default implementation of MangoQuery. Setup as spring bean that is used by all the repos
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class DefaultQueryService<D> implements QueryService<D> {

    @Autowired
    MangoBuilder mangoBuilder

    Class<D> entityClass

    DefaultQueryService(){}

    DefaultQueryService(Class<D> entityClass){
        this.entityClass = entityClass
    }

    public static <D> DefaultQueryService<D> of(Class<D> entityClass){
        def qsInstance = new DefaultQueryService(entityClass)
        AppCtx.autowire(qsInstance) //wire up the @Autowired
        return qsInstance
    }

    @Override
    MangoDetachedCriteria<D> query(Map params, @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {
        return query(QueryArgs.of(params), closure)
    }

    /**
     *  Builds detached criteria for repository's domain based on mango criteria language
     *
     * @param entityClass the base entity class
     * @param qargs the QueryArgs with the prepared criteria in it.
     * @param closure extra criterai closure
     * @return the detached criteria to call list or get on
     */
    @Override
    MangoDetachedCriteria<D> query(QueryArgs qargs, @DelegatesTo(MangoDetachedCriteria) Closure applyClosure = null) {
        MangoDetachedCriteria<D> mangoCriteria = createCriteria(qargs, applyClosure)
        applyCriteria(mangoCriteria)
        // MangoDetachedCriteria<D> mangoCriteria = mangoBuilder.createCriteria(getEntityClass() as Class<D>, qargs, applyClosure)
        // mangoBuilder.applyCriteria(mangoCriteria)
        return mangoCriteria
    }

    /**
     * passes on to the mangoBuilder, allows sub-classes to override and modify
     */
    @Override
    MangoDetachedCriteria<D> createCriteria(QueryArgs qargs, Closure applyClosure){
        return mangoBuilder.createCriteria(getEntityClass() as Class<D>, qargs, applyClosure)
    }

    /**
     * passes on to the mangoBuilder, allows sub-classes to override and modify
     */
    @Override
    void applyCriteria(MangoDetachedCriteria<D> mangoCriteria){
        publishCriteriaEvent(mangoCriteria)
        try {
            mangoBuilder.applyCriteria(mangoCriteria)

            //DateTimeParseException would get thrown when a date value is bad
            //IllegalArgumentException gets thrown when trying query a non existing association field
            //GroovyCastException gets thrown when value doesnt match field type, eg string val for an int type field
        } catch (Exception ex) {
            //See #1925 - Catch bad qargs
            throw DataProblem.ex("Invalid query string - $ex.message")
        }
    }

    protected void publishCriteriaEvent(MangoDetachedCriteria<D> mangoCriteria){
        AppCtx.publishEvent(new MangoQueryCriteriaEvent<>(getEntityClass(), mangoCriteria))
    }

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
    public List pagedList(MangoDetachedCriteria criteria, Pager pager) {
        Map args = [max: pager.max, offset: pager.offset]
        List resList
        if(criteria.projections){
            resList =  criteria.mapList(args)
        } else {
            //return standard list
            resList =  criteria.list(args)
        }
        return resList
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
    Map countTotals(Map params = [:], List<String> sums, @DelegatesTo(MangoDetachedCriteria) Closure closure = null) {

        DetachedCriteria mangoCriteria = query(params, closure)

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
