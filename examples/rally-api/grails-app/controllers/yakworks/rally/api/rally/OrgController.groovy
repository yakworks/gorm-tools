/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api.rally

import groovy.transform.CompileStatic

import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rest.gorm.controller.CrudApiController

import static org.springframework.http.HttpStatus.CREATED

@CompileStatic
class OrgController implements CrudApiController<Org> {
    static String namespace = 'rally'

    // XXX @SUD why doesn't error handling pick this up without being wrapped in try/catch?
    // @Override
    // def post() {
    //     Map qParams = getParamsMap()
    //     Map entityMap = getCrudApi().create(bodyAsMap(), qParams)
    //     respondWith(entityMap, [status: CREATED, params: qParams])
    // }

    // XXX this works
    // @Override
    // def post() {
    //     try {
    //         assert "foo" == "bar"
    //     } catch (Exception | AssertionError e) {
    //         handleException(e)
    //     }
    // }

}
