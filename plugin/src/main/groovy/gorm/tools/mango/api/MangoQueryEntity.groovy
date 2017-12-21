package gorm.tools.mango.api

import grails.gorm.DetachedCriteria
import groovy.transform.CompileStatic

import javax.persistence.Transient

/**
 * a trait for gorm domain entities to give them quick search and mango query statics
 */
@CompileStatic
trait MangoQueryEntity {

    @Transient
    static List<String> quickSearchFields = []

    static abstract MangoQueryTrait getMangoQueryTrait()

    static DetachedCriteria buildCriteria(Map params = [:], Closure closure = null) {
        getMangoQueryTrait().buildCriteria(params, closure)
    }

    /**
     * List of entities restricted by mango map and criteria closure
     *
     * @param params mango language criteria map
     * @param closure additional restriction for criteria
     * @return query of entities restricted by mango params
     */
    static List query(Map params = [:], Closure closure = null) {
        getMangoQueryTrait().query(params, closure)
    }
}
