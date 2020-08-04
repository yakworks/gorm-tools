/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

//import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.Pager
import gorm.tools.mango.api.QueryMangoEntityApi
import gorm.tools.repository.api.RepositoryApi
import grails.gorm.DetachedCriteria

/**
 *  Adds controller methods for list
 *
 *  Created by alexeyzvegintcev.
 */
@CompileStatic
trait MangoControllerApi {

    abstract RepositoryApi getRepo()

    //just casts the repo to QueryMangoEntityApi
    QueryMangoEntityApi getMangoApi(){
        return (getRepo() as QueryMangoEntityApi)
    }

    DetachedCriteria buildCriteria(Map criteriaParams = [:], Map params = [:], Closure closure = null) {
        getMangoApi().buildCriteria(criteriaParams + params, closure)
    }

    List query(Map criteriaParams = [:], Map params = [:], Closure closure = null) {
        getMangoApi().query(criteriaParams + params, closure)
    }

    List query(Pager pager, Map p = [:]) {
        ['max', 'offset', 'page'].each{ String k ->
            p[k] = pager[k]
        }
        //fix up quickSearch
        if(p['q']) p['$q'] = p['q']
        //clean up sort if passed the jqgrid way
        if(p['sort'] && p['order']) {
            Map newSort = [:]
            newSort[p.sort] = p.order
            p.sort = newSort
        }
        getMangoApi().query(p)
    }

}
