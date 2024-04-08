/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.problem

import groovy.json.JsonException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSourceResolvable
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

import gorm.tools.repository.errors.EmptyErrors
import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.api.problem.GenericProblem
import yakworks.api.problem.Problem
import yakworks.api.problem.ThrowableProblem
import yakworks.api.problem.UnexpectedProblem
import yakworks.api.problem.Violation
import yakworks.api.problem.ViolationFieldError
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemCodes
import yakworks.i18n.icu.ICUMessageSource

/**
 * Service to prepare ApiError / ApiValidationError for given a given exception
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@Slf4j
@CompileStatic
class ProblemHandler {

    @Autowired ICUMessageSource messageSource

    static {
        stackTraceUtilsDefaultFilters()
    }

    GenericProblem handleException(Class entityClass, Throwable e) {
        handleException(e, entityClass.simpleName)
    }

    /**
     * Prepares Problem for given entity and exception
     * - Problem(status:422) for ValidationException
     * - Problem(status:400) for DataAccessException
     * - Problem(status:404) for NotFoundException
     * - Problem(status:500) for other exceptions
     *
     * @param simpleName used for validation conversion
     * @param Exception e
     * @return ApiError
     */
    GenericProblem handleException(Throwable e, String simpleName = null) {
        // default error status code is 422
        ApiStatus status400 = HttpStatus.BAD_REQUEST
        ApiStatus status404 = HttpStatus.NOT_FOUND
        ApiStatus status422 = HttpStatus.UNPROCESSABLE_ENTITY

        if (e instanceof ValidationProblem.Exception) {
            def valProblem = e.getValidationProblem()
            if (valProblem.errors instanceof EmptyErrors) {
                //this is some other exception wrapped in validation exception
                valProblem.detail(e.cause?.message)
            }
            valProblem.violations(transateErrorsToViolations(valProblem.errors))
            return valProblem
        }
        else if (e instanceof ThrowableProblem) {
            return (GenericProblem) e.problem
        }
        else if (e instanceof GenericProblem) {
            return (GenericProblem) e
        }
        else if (e instanceof grails.validation.ValidationException
            || e instanceof org.grails.datastore.mapping.validation.ValidationException) {
            return buildFromErrorException(e, simpleName)
        } else if (e instanceof IllegalArgumentException) {
            //We use this all over to double as a validation error, Validate.notNull for example.
            return Problem.of('error.illegalArgument').status(status400).detail(e.message)
        } else if (e instanceof DataAccessException) {
            //if its an unique index problem then 90% of time its validation issue and expected.
            if (isUniqueIndexViolation((DataAccessException) e)) {
                return DataProblemCodes.UniqueConstraint.of(e)
            } else {
                //For now turn to warn in case we want to turn it off.
                String rootMessage = e.rootCause?.getMessage()
                String msgInfo = "===  message: ${e.message} \n === rootMessage: ${rootMessage} "

                log.error("MAYBE UNEXPECTED? Data Access Exception ${msgInfo}", StackTraceUtils.deepSanitize(e))
                return DataProblem.of(e)
            }
        } else if (e instanceof HttpMessageNotReadableException || e instanceof JsonException) {
            //this happens if request contains bad data / malformed json. we dont want to log stacktraces for these as they are expected
            return DataProblem.of(e)
        }
        else {
            return handleUnexpected(e)
        }
    }

    GenericProblem handleUnexpected(Throwable e){
        log.error("UNEXPECTED Internal Server Error\n${e.message}", StackTraceUtils.deepSanitize(e))
        if (e instanceof GenericProblem) {
            return (GenericProblem) e
        }
        else if (e instanceof ThrowableProblem) {
            return (GenericProblem) e.problem
        }
        else if (e instanceof NullPointerException) {
            //deal with the dreaded null pointer
            String stackLine1 = e.stackTrace[0].toString()
            return new UnexpectedProblem().cause(e).detail("NullPointerException at ${stackLine1}")
        }
        else {
            return new UnexpectedProblem().cause(e).detail(e.message)
        }
    }

    ValidationProblem buildFromErrorException(Throwable valEx, String entityName = null) {
        Errors ers = valEx['errors'] as Errors
        def valProb = ValidationProblem.of(valEx).errors(ers)
        if(entityName) valProb.name(entityName)
        return valProb.violations(transateErrorsToViolations(ers))
    }

    String getMsg(MessageSourceResolvable msr) {
        //FIXME this should be generalized somehwere?
        try {
            return messageSource.getMessage(msr)
        }
        catch (e) {
            return msr.codes[0]
        }
    }

    /**
     * Returns list of errors in the format [{field:name, message:error}]
     * @param errs the erros object to convert
     */
    List<Violation> transateErrorsToViolations(Errors errs) {
        List<ViolationFieldError> errors = []
        if(!errs?.allErrors) return errors as List<Violation>

        for (ObjectError err : errs.allErrors) {
            ViolationFieldError fieldError = ViolationFieldError.of(err.code, getMsg(err))
            if (err instanceof FieldError) fieldError.field = err.field
            errors << fieldError
        }
        return errors as List<Violation>
    }

    //Unique index unique constraint or primary key violation
    @SuppressWarnings('BracesForIfElse')
    static String isUniqueIndexViolation(DataAccessException dax) {
        if(!dax.rootCause) return null
        String rootMessage = dax.rootCause.message
        if (rootMessage.contains("Unique index or primary key violation") || //mysql and H2
            rootMessage.contains("Duplicate entry") || //mysql
            rootMessage.contains("Violation of UNIQUE KEY constraint") || //sql server
            rootMessage.contains("unique constraint")) {
            return rootMessage
        } else {
            return null
        }
    }

    static String isForeignKeyViolation(DataAccessException dax) {
        if (!dax.rootCause || !(dax instanceof DataIntegrityViolationException)) return null
        String rootMessage = dax.rootCause.message.toLowerCase()
        //postgres and H2 - if its DataIntegrityViolationException and contains keyword 'foreign key' thn its fk violation
        if (rootMessage.contains("foreign key")) {
            return rootMessage
        } else {
            return null
        }
    }

    /**
     * Broken pipe exception happens when client has closed the socket and server tries to write/send any response byte on the output stream.
     * Server Can write nothing to output stream once we encounter Broken pipe exception
     */
    static boolean isBrokenPipe(Exception ex) {
        return ex.message && ex.message.toLowerCase().contains("broken pipe")
    }

    //Legacy from ValidationException
    static String formatErrors(Errors errors, String msg) {
        String ls = System.getProperty("line.separator");
        StringBuilder b = new StringBuilder();
        if (msg != null) {
            b.append(msg).append(" : ") //.append(ls);
        }

        for (ObjectError error : errors.getAllErrors()) {
            b.append(ls)
                .append(" - ")
                .append(error)
                .append(ls);
        }
        return b.toString();
    }

    @SuppressWarnings(['BooleanMethodReturnsNull'])
    static void stackTraceUtilsDefaultFilters(){
        StackTraceUtils.addClassTest { String className ->
            for (String groovyPackage : (NOISY_PACKAGES + NOISY_TEST_PACKAGES)) {
                if (className.startsWith(groovyPackage)) {
                    return false
                }
            }
            return null
        }
    }

    //the list of packages to summarize up so logging trace is not so noisy. only logs one line if multiples start with these
    public static List NOISY_PACKAGES = [
        'jdk.internal.reflect.NativeMethodAccessorImpl',
        'jdk.internal.reflect.DelegatingMethodAccessorImpl',
        'jdk.internal.reflect.GeneratedMethodAccessor',
        'org.springframework.web.filter.OncePerRequestFilter',
        'org.springframework.web.filter.CharacterEncodingFilter',
        'org.springframework.web.filter.DelegatingFilterProxy',
        'org.springframework.security.web',
        'org.grails.core.DefaultGrailsControllerClass',
        'org.grails.web.servlet.mvc.GrailsWebRequestFilter',
        'org.grails.web.filters.HiddenHttpMethodFilter',
        'org.grails.datastore.mapping.reflect.FieldEntityAccess',
        'org.apache.catalina.core',
        'org.apache.tomcat.websocket.server.WsFilter',
        'org.apache.tomcat.util.net',
        'org.apache.tomcat.util.threads',
        'org.apache.coyote'
    ];

    public static List NOISY_TEST_PACKAGES = [
        'jdk.internal.reflect.NativeConstructorAccessorImpl',
        'org.spockframework.runtime',
        'org.spockframework.util.ReflectionUtil',
        'org.spockframework.junit4.ExceptionAdapterInterceptor',
        'org.junit.platform.engine.support.hierarchical',
        'org.junit.platform.launcher.core.EngineExecutionOrchestrator',
        //'org.gradle',

    ];

}
