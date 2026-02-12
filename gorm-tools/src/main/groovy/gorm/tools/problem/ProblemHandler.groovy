/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.problem

import groovy.json.JsonException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.codehaus.groovy.runtime.StackTraceUtils
import org.hibernate.QueryTimeoutException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSourceResolvable
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.Errors
import org.springframework.validation.ObjectError
import org.springframework.web.HttpRequestMethodNotSupportedException

import gorm.tools.repository.errors.EmptyErrors
import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.api.problem.GenericProblem
import yakworks.api.problem.Problem
import yakworks.api.problem.ThrowableProblem
import yakworks.api.problem.UnexpectedProblem
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemCodes
import yakworks.i18n.icu.ICUMessageSource
import yakworks.message.MsgServiceRegistry

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
        //setup default class filtering for making stack trace less noisy
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
        ApiStatus status401 = HttpStatus.UNAUTHORIZED

        if (e instanceof ValidationProblem.Exception) {
            def valProblem = e.getValidationProblem()
            if (valProblem.errors instanceof EmptyErrors) {
                //this is some other exception wrapped in validation exception
                valProblem.detail(e.cause?.message)
            }
            //translate the errors
            if(!valProblem.violations && valProblem.errors?.hasErrors()){
                //we do this late, not done when created with RepoExceptionSupport
                valProblem.violations(ValidationProblem.transateErrorsToViolations(valProblem.errors))
            }
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
        }
        else if(isQueryTimeout(e)) {
            return DataProblem.of("error.query.timeout")
        }
        else if (e instanceof DataAccessException) {
            return buildFromDataAccessException(e)
        }
        else if (e instanceof HttpMessageNotReadableException || e instanceof JsonException
            || e instanceof HttpRequestMethodNotSupportedException) {
            //this happens if request contains bad data / malformed json. we dont want to log stacktraces for these as they are expected
            return DataProblem.of(e)
        }
        else if(e instanceof AssertionError) {
            return DataProblem.of(e)
        }
        else {
            return handleUnexpected(e)
        }
    }

    GenericProblem handleUnexpected(Throwable e){
        log.error("UNEXPECTED Internal Server Error\n${e.message}", deepSanitize(e))
        if (e instanceof GenericProblem) {
            return (GenericProblem) e
        }
        else if (e instanceof ThrowableProblem) {
            return (GenericProblem) e.problem
        }
        else if (e instanceof NullPointerException) {
            //deal with the dreaded null pointer
            //Check if there's stacktrace, in certain cases stacktrace is coming up empty, which is causing Arrayoutofbound ex - see #2712
            String stackLine1 = e.stackTrace ? "at ${e.stackTrace[0].toString()}" : ""
            return new UnexpectedProblem().cause(e).detail("NullPointerException $stackLine1")
        }
        else {
            return new UnexpectedProblem().cause(e).detail(e.message)
        }
    }

    //XXX for OptimisticLockingFailureException there are times when its valid I think
    // but then times when its our processes (autocash for example). How do we parse that out?
    // OptimisticLockingFailureException is a DataAccessException so it hits the else below
    // and we always log it out as error.
    static DataProblem buildFromDataAccessException(DataAccessException e) {
        // Root of the hierarchy of data access exceptions
        if(isUniqueIndexViolation((DataAccessException) e)){
            return DataProblemCodes.UniqueConstraint.of(e)
        }
        else if(isForeignKeyViolation((DataAccessException) e)){
            return DataProblemCodes.ReferenceKey.of(e)
        }
        else {
            //For now turn to warn in case we want to turn it off.
            String rootMessage = e.rootCause?.getMessage()
            String msgInfo = "===  message: ${e.message} \n === rootMessage: ${rootMessage} "

            log.error("MAYBE UNEXPECTED? Data Access Exception ${msgInfo}", deepSanitize(e))
            return DataProblem.of(e)
        }
    }

    ValidationProblem buildFromErrorException(Throwable valEx, String entityName = null) {
        Errors ers = valEx['errors'] as Errors
        def valProb = ValidationProblem.of(valEx).errors(ers)
        if(entityName) valProb.name(entityName)
        return valProb.violations(ValidationProblem.transateErrorsToViolations(ers))
    }

    static String getMsg(MessageSourceResolvable msr) {
        //FIXME this should be generalized somehwere?
        try {
            //cast so we can use the getMessage(MessageSourceResolvable resolvable), which works
            ICUMessageSource msgService = MsgServiceRegistry.service as ICUMessageSource//get static msgService that should have been set in icu4j plugin
            return msgService.getMessage(msr)
        }
        catch (e) {
            return msr.defaultMessage
        }
    }

    /**
     * returns true if the exception is a psql query timeout exception
     */
    static boolean isQueryTimeout(Throwable ex) {
        //Criteria/Mango throws QueryTimeoutException, jdbcTemplate throws DataAccessResourceFailureException
        return (ex instanceof QueryTimeoutException) ||
            (ex instanceof DataAccessResourceFailureException && ex.message.contains('canceling statement due to statement timeout"'))
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
    static boolean isBrokenPipe(Throwable ex) {
        return ex.message && ex.message.toLowerCase().contains("broken pipe")
    }

    /**
     * Handles Access denied exception, which is thrown when user doesnt have required roles/authority to access a method
     */
    static GenericProblem handleAccessDenied(Exception ex) {
        return Problem.of('error.unauthorized').status(HttpStatus.UNAUTHORIZED).detail(ex.message)
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

    public static Throwable deepSanitize(Throwable t) {
        StackTraceUtils.deepSanitize(t)
    }

    //setup default class filtering for making stack trace less noisy
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
