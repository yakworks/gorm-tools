/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api.rally

import groovy.transform.CompileStatic

import yakworks.api.problem.data.DataProblem
import yakworks.rally.orgs.model.Org
import yakworks.rest.gorm.controller.CrudApiController

import static org.springframework.http.HttpStatus.OK

@SuppressWarnings(['ThrowRuntimeException'])
@CompileStatic
class ExceptionTestController implements CrudApiController<Org> {
    static String namespace = 'rally'

    //here, to test exception handling
    @SuppressWarnings(['DeadCode'])
    def runtimeException() {
        //anything that extends from Exception will be handled automatically.
        throw new RuntimeException("Test error")
        respondWith([ok:true], [status: OK])
    }

    //add test here
    @SuppressWarnings(['DeadCode'])
    def dataProblem() {
        //anything that extends from Exception will be handled automatically.
        throw DataProblem.ex("test")
        respondWith([ok:true], [status: OK])
    }


    @SuppressWarnings(['ComparisonOfTwoConstants'])
    def throwable() {
        //this one will be handled by error.hbs
        assert "foo" == "bar"
        respondWith([ok:true], [status: OK])
    }
}
