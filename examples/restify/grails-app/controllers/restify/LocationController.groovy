/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import groovy.transform.CompileStatic

import gorm.tools.rest.controller.RestApiRepoController
import yakworks.taskify.domain.*

import static org.springframework.http.HttpStatus.CREATED

@CompileStatic
class LocationController extends RestApiRepoController<Location> {

    LocationController() {
        super(Location, false)
    }

    def post() {
        Map q = getDataMap()
        q.street = q.street == null ? null : "foo street"
        Location instance = getRepo().create(q)
        respond instance, [status: CREATED] //201
    }

}
