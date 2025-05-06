/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import groovy.transform.CompileStatic

import org.springframework.dao.DataAccessException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.validation.Errors

import gorm.tools.problem.ProblemHandler
import gorm.tools.problem.ValidationProblem
import yakworks.api.problem.Problem

/**
 * Handler and translator for exceptions thrown by the Repository
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class RepoExceptionSupport {

    // XXX Do we still need this? this was done well before the ProblemHandler, can we just use the ProblemHandler?
    //  not clear what this provides over ProblemHandler,
    //  if we need this lets get description in javadocs above and explain why we need this or why its beneficcial

    /**
     * Translates grails ValidationException and springs DataAccessException and DataIntegrityViolationException
     * into our EntityValidationException so we have better information in the exception when its thrown and rolled back
     *
     * @param ex the RuntimeException that was thrown
     * @param entity the entity this was thrown for
     * @return the new EntityValidationException or the OptimisticLockingFailureException with a betters message if thats was what
     *         was thrown originally
     */
    static RuntimeException translateException(RuntimeException ex, Object entity) {
        /*
         * We need to check for EntityValidationException first and return it back without changes,
         * because in case "ex" is the EntityValidationException, it will be re-created with "notSaved" message.
         * This way we can lose the original message that is stored in EntityValidationException.
         * It happens because EntityValidationException is inherited from DataIntegrityViolationException and DataAccessException,
         * thus checks for these exceptions also cover EntityValidationException case.
         */
        //if its an instance of Problem then we dont need to transalate
        if (ex instanceof Problem ) {
            return ex
        }
        else if (ex instanceof grails.validation.ValidationException
            || ex instanceof org.grails.datastore.mapping.validation.ValidationException) {
            Errors ers = ex['errors'] as Errors
            def valProb = ValidationProblem.of(ex).entity(entity).errors(ers)
            return valProb.toException()
        }
        // else if (ex instanceof grails.validation.ValidationException) {
        //     def ve = (grails.validation.ValidationException) ex
        //     return ValidationProblem.of(entity, ve).errors(ve.errors).toException()
        // }
        // else if (ex instanceof org.grails.datastore.mapping.validation.ValidationException) {
        //     // Gorm's stock ValidationException
        //     def ve = (org.grails.datastore.mapping.validation.ValidationException) ex
        //     return ValidationProblem.of(entity, ve).errors(ve.errors).toException()
        // }
        else if (ex instanceof OptimisticLockingFailureException) {
            return ex //just return unchanged
            // return new OptimisticLockingFailureException(RepoMessage.optimisticLockingFailure(entity, true).defaultMessage as String)
        }
        else if (ex instanceof DataAccessException) {
            return ProblemHandler.buildFromDataAccessException((DataAccessException)ex).entity(entity).toException()
        }
        return ex
    }

}
