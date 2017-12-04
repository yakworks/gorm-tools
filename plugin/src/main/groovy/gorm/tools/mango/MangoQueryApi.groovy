package gorm.tools.mango

import grails.gorm.DetachedCriteria
import groovy.transform.CompileDynamic
import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.factory.annotation.Autowired

trait MangoQueryApi<D extends GormEntity> {

    abstract Class<D> getDomainClass()

    @Autowired
    MangoQuery mangoQuery

    /**
     * Builds detached criteria for dao's domain based on mango criteria language and additional criteria
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return Detached criteria build based on mango language params and criteria closure
     */
    @CompileDynamic
    DetachedCriteria buildCriteria( Map params=[:], Closure closure=null) {
        mangoQuery.buildCriteria(getDomainClass(), params, closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    @CompileDynamic
     List query(Map params=[:], Closure closure=null){
         mangoQuery.query(getDomainClass(), params, closure)
     }

}
