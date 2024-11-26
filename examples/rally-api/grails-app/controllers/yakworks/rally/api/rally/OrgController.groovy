/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api.rally

import groovy.transform.CompileStatic

import yakworks.api.problem.data.DataProblem
import yakworks.rally.orgs.model.Org
import yakworks.rest.gorm.controller.CrudApiController

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK

@CompileStatic
class OrgController implements CrudApiController<Org> {
    static String namespace = 'rally'

    @Override
    def post() {
        Map qParams = getParamsMap()
        Map entityMap = getCrudApi().create(bodyAsMap(), qParams).asMap()
        respondWith(entityMap, [status: CREATED, params: qParams])
    }

    //here, to test exception handling
    def exception() {
        throw DataProblem.ex("test")
        respondWith([ok:true], [status: OK])
    }

    def throwable() {
        assert "foo" == "bar"
        respondWith([ok:true], [status: OK])
    }
}
