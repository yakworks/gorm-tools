package gorm.tools.mango

import gorm.tools.Pager
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import org.springframework.beans.factory.annotation.Value

@Transactional(readOnly = true)
class MangoQuery implements MangoQueryApi {

    @Value('${gorm.tools.mango.criteriaKeyName:criteria}') //gets criteria keyword from config, if there is no, then uses 'criteria'
    String criteriaKeyName

    /**
     * Builds detached criteria for dao's domain based on mango criteria language and additional criteria
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    @CompileDynamic
    DetachedCriteria buildCriteria(Class domainClass, Map params = [:], Closure closure = null) {
        Map criteria
        if (params[criteriaKeyName] instanceof String) {
            JSON.use('deep')
            criteria = JSON.parse(params[criteriaKeyName]) as Map
        } else {
            criteria = params[criteriaKeyName] as Map ?: [:]
        }
        if (params.containsKey('sort')) {
            criteria['$sort'] = params['sort']
        }
        MangoBuilder.build(domainClass, criteria, closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    @CompileDynamic
    List query(Class domainClass, Map params = [:], Closure closure = null) {
        query(buildCriteria(domainClass, params, closure), params, closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    @CompileDynamic
    List query(DetachedCriteria criteria, Map params = [:], Closure closure = null) {
        Pager pager = new Pager(params)
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
    @CompileDynamic
    Map countTotals(Class domainClass, Map params = [:], List<String> sums, Closure closure = null) {
        DetachedCriteria mangoCriteria = buildCriteria(domainClass, params, closure)

        List totalList
        totalList = mangoCriteria.list {
            projections {
                for (String sumField : sums) {
                    sum(sumField)
                }
            }
        }

        Map result = [:]
        sums.eachWithIndex { String name, i ->
            result[name] = totalList[0][i]
        }
        return result
    }
}
