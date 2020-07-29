/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.controller

import groovy.transform.CompileDynamic

import gorm.tools.repository.api.RepositoryApi
import grails.gorm.DetachedCriteria

/**
 *  Adds controller methods for list
 *
 *  Created by alexeyzvegintcev.
 */
@CompileDynamic
trait MangoControllerApi {

    abstract RepositoryApi getRepo()

    DetachedCriteria buildCriteria(Map criteriaParams = [:], Map params = [:], Closure closure = null) {
        getRepo().buildCriteria(criteriaParams + params, closure)
    }

    List query(Map criteriaParams = [:], Map params = [:], Closure closure = null) {
        getRepo().query(criteriaParams + params, closure)
    }

}
