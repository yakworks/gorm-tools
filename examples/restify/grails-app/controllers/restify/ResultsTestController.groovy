/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import groovy.transform.CompileStatic

import gorm.tools.rest.controller.RestRepoApiController
import gorm.tools.support.Results
import yakworks.rally.orgs.model.Org

import static org.springframework.http.HttpStatus.CREATED

@CompileStatic
class ResultsTestController implements RestRepoApiController<Org> {

    @Override
    def get() {
        respond(Results.OK())
    }

}
