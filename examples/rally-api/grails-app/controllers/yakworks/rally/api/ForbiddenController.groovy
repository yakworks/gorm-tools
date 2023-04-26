package yakworks.rally.api


import groovy.transform.CompileStatic

import yakworks.api.problem.Problem
import yakworks.rest.gorm.controller.RestApiController

/**
 * 403 forbidden responder
 */
@SuppressWarnings(['Println'])
@CompileStatic
class ForbiddenController implements RestApiController {

    def index() {
        //model get forwarded to requests attributes
        Exception ex = request['exception'] as Exception
        def problem = Problem.of('error.forbidden')
            .status(403)
            .title("Access Forbidden")
            .detail(ex?.message)
        respondWith problem
    }
}
