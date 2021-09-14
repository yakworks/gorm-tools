/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import groovy.transform.CompileStatic

import org.springframework.context.MessageSource
import org.springframework.dao.DataAccessException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import gorm.tools.beans.AppCtx
import gorm.tools.repository.RepoMessage

/**
 * Handler and translator for exceptions thrown by the Repository
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class RepoExceptionSupport {

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
        if (ex instanceof EntityValidationException) {
            return ex
        }
        else if (ex instanceof grails.validation.ValidationException) {
            def ve = (grails.validation.ValidationException) ex
            return new EntityValidationException(RepoMessage.validationError(entity), entity, ve.errors, ve)
        }
        else if (ex instanceof org.grails.datastore.mapping.validation.ValidationException) {
            // Gorm's stock ValidationException
            def ve = (org.grails.datastore.mapping.validation.ValidationException) ex
            return new EntityValidationException(RepoMessage.validationError(entity), entity, ve.errors, ve)
        }
        else if (ex instanceof OptimisticLockingFailureException) {
            return ex //just return unchanged
            // return new OptimisticLockingFailureException(RepoMessage.optimisticLockingFailure(entity, true).defaultMessage as String)
        }
        else if (ex instanceof DataAccessException) {
            // Root of the hierarchy of data access exceptions
            return new EntityValidationException(RepoMessage.notSaved(entity), entity, null, ex)
        }
        return ex
    }

    /**
     * Returns list of errors in the format [{field:name, message:error}]
     * @param errs the erros object to convert
     */
    static List<Map<String, String>> toErrorList(Errors errs) {
        List<Map<String, String>> errors = []
        MessageSource messageSource =  AppCtx.getCtx()
        errs.allErrors.each {def err ->
            Map m = [message:messageSource.getMessage(err, RepoMessage.defaultLocale())]
            if(err instanceof FieldError) m['field'] = err.field
            errors << m
        }
        return errors
    }


}
