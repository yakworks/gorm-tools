/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileStatic

import yakworks.gorm.rest.controller.RestRepoApiController
import yakworks.rally.orgs.model.Location

import static org.springframework.http.HttpStatus.CREATED

@CompileStatic
class LocationController implements RestRepoApiController<Location> {
    static String namespace = 'rally'
    def post() {
        Map q = bodyAsMap()
        q.street = q.street == null ? null : "foo street"
        Location instance = getRepo().create(q)
        respond instance, [status: CREATED] //201
    }

}
