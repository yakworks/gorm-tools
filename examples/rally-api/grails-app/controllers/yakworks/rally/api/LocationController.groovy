/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileStatic

import yakworks.rally.orgs.model.Location
import yakworks.rest.gorm.controller.CrudApiController

import static org.springframework.http.HttpStatus.CREATED

@CompileStatic
class LocationController implements CrudApiController<Location> {
    static String namespace = 'rally'

    @Override
    def post() {
        Map q = bodyAsMap()
        q.street = q.street == null ? null : "foo street"
        Map emap = getCrudApi().create(q, params)
        respondWith emap, [status: CREATED] //201
    }

}
