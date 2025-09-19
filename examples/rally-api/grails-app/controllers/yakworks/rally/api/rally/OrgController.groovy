/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api.rally

import groovy.transform.CompileStatic

import yakworks.rally.orgs.model.Org
import yakworks.rest.gorm.controller.CrudApiController

import static org.springframework.http.HttpStatus.CREATED

@CompileStatic
class OrgController implements CrudApiController<Org> {
    static String namespace = 'rally'

    @Override
    def post() {
        Map qParams = getParamsMap()
        Map entityMap = getCrudApi().create(bodyAsMap(), qParams).asMap()
        respondWith(entityMap, [status: CREATED, params: qParams])
    }

    //just here to hit rpc during tests
    def rpc(){
        Map qParams = getParamsMap()
        String op = qParams['op']
        respond([ok:true, rpc:op])
    }
}
