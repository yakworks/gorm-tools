package gorm.tools.mango

import grails.gorm.DetachedCriteria

interface MangoQueryApi {

    /**
     * Builds detached criteria for dao's domain based on mango criteria language and additional criteria
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    DetachedCriteria buildCriteria(Class domainClass, Map params, Closure closure)

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    List query(Class domainClass, Map params, Closure closure)
}
