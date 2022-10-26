package yakworks.rally.api


import groovy.transform.CompileStatic

import yakworks.api.problem.Problem
import yakworks.rest.gorm.controller.RestApiController

/**
 * 403 forbidden responder
 */
@SuppressWarnings(['Println'])
@CompileStatic
class ErrorProblemController implements RestApiController {

    def index() {
        //model get forwarded to requests attributes
        Exception ex = request['exception'] as Exception
        def problem = Problem.of('error.forbidden')
            .status(403)
            .title("Access Forbidden")
            .detail(ex?.message)
        respond problem
    }

    def notFound404() {
        //model get forwarded to requests attributes
        Exception ex = request['exception'] as Exception
        def problem = Problem.of('error.notFound')
            .status(404)
            .title("Not Found")
            .detail(ex?.message)
        respond problem
    }
}
