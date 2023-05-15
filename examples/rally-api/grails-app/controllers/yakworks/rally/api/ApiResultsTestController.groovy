/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileStatic

import yakworks.api.ApiResults
import yakworks.rally.orgs.model.Org
import yakworks.rest.gorm.controller.RestRepoApiController

@CompileStatic
class ApiResultsTestController implements RestRepoApiController<Org> {

    @Override
    def get() {
        respondWith(ApiResults.OK())
    }

}
