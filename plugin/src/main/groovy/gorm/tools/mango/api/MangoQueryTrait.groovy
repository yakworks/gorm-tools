package gorm.tools.mango.api

import grails.gorm.DetachedCriteria
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@CompileStatic
trait MangoQueryTrait {

    abstract Class getDomainClass()

    @Autowired
    @Qualifier("mango")
    MangoQueryApi mangoQuery

    /**
     * Builds detached criteria for repository's domain based on mango criteria language and additional criteria
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    DetachedCriteria buildCriteria(Map params = [:], Closure closure = null) {
        getMangoQuery().buildCriteria(getDomainClass(), params, closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    List query(Map params = [:], Closure closure = null) {
        getMangoQuery().query(getDomainClass(), params, closure)
    }

}
