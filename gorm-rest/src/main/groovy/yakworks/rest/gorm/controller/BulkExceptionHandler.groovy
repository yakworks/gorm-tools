/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.controller

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.problem.ProblemHandler
import yakworks.api.problem.Problem

/**
 * Special handler for bulk operations, so that we can log/highight every bulk error we send.
 * Its here, because we cant have more thn one exception handler for "Exception" in controller
 */
@Slf4j
@CompileStatic
class BulkExceptionHandler {

    ProblemHandler problemHandler
    Class entityClass // the domain class this is for

    BulkExceptionHandler(Class entityClass){
        this.entityClass = entityClass
    }

    public static BulkExceptionHandler of(Class entityClass, ProblemHandler problemHandler){
        def bcs = new BulkExceptionHandler(entityClass)
        bcs.problemHandler = problemHandler
        return bcs
    }

    /**
     * Special handler for bulk operations, so that we can log/highight every bulk error we send.
     * Its here, because we cant have more thn one exception handler for "Exception" in controller
     */
    Problem handleBulkOperationException(HttpServletRequest req, Throwable e) {
        Problem apiError = problemHandler.handleException(getEntityClass(), e)
        if (apiError.status.code == 500) {
            String requestInfo = "requestURI=[${req.requestURI}], method=[${req.method}], queryString=[${req.queryString}]"
            log.warn("⛔️ 👉 Bulk operation exception ⛔️ \n $requestInfo \n $apiError.cause?.message")
        }
        return apiError
    }
}
