package gorm.tools.mango.api

import grails.gorm.DetachedCriteria
import groovy.transform.CompileStatic

@CompileStatic
trait MangoQueryApi {

    /**
     * Builds detached criteria for repository's domain based on mango criteria language and additional criteria
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    abstract DetachedCriteria buildCriteria(Class domainClass, Map params, Closure closure)

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    abstract List query(Class domainClass, Map params, Closure closure)
}
