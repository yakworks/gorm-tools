/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import java.beans.Introspector

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormValidateable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.AbstractBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import grails.gorm.validation.ConstrainedProperty
import yakworks.i18n.icu.ICUMessageSource

/**
 * A helper trait for a repo to allow rejecting values for validation
 * based on how the org.springframework.validation.Errors works and
 * what is done in the org.grails.datastore.gorm.validation.constraints.AbstractConstraint
 *
 * why: the constraints in Gorm work fine but are messy and difficult to manage for anything but the out of the box
 * this allows us to rejectValues in the same way as the constraints do but tied to different config settings
 * and complicate business logic.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait RepoEntityErrors<D> {

    abstract Class<D> getEntityClass()

    @Autowired(required = false)
    ICUMessageSource messageSource

    /**
     * validates that the prop is not null and registers error if so
     */
    boolean validateNotNull(GormValidateable target, String propName, Errors errors = null){
        if (target[propName] == null) {
            rejectNullValue(target, propName, errors)
            return false
        }
        return true
    }

    // shortcut to reject a null value using the ConstrainedProperty defaults
    void rejectNullValue(GormValidateable target, String propName, Errors errors = null) {
        rejectValue(target, errors, propName, null, ConstrainedProperty.NULLABLE_CONSTRAINT, ConstrainedProperty.DEFAULT_NULL_MESSAGE_CODE)
    }

    //based on Errors and AbstractConstraint to keep it consistent
    void rejectValue(GormValidateable target, String propName, Object val, String code, String defaultMessageCode = null, Object argsOverride = null) {
        rejectValueWithMessage(target, target.errors, propName, val, code, getDefaultMessage(defaultMessageCode), argsOverride)
    }

    //based on Errors and AbstractConstraint to keep it consistent
    void rejectValue(GormValidateable target, Errors errors, String propName, Object val, String code,
                     String defaultMessageCode = null, Object argsOverride = null) {
        rejectValueWithMessage(target, errors, propName, val, code, getDefaultMessage(defaultMessageCode), argsOverride)
    }

    //copied in from AbstractConstraint to keep it consistent
    /**
     * Build errors message, based on AbstractConstraint to keep it consistent
     * passing in an errors will keep properties nested. so if target is Info is an association of Org
     * or Info belongs to Org, then errors would have nested dot notation such as 'info.email' as error.
     *
     * @param target - the target object for error, can be nested
     * @param errors - if part of a chain then this is the parent errors
     * @param propName - the name of the property on target
     * @param val - the value that is rejected
     * @param code - message properties code
     * @param defaultMessage - default message if any
     * @param argsOverride - overrides for the arguments that are to be passed into the getMessage
     */
    void rejectValueWithMessage(GormValidateable target, Errors errors, String propName, Object val, String code,
                                String defaultMessage = null, Object argsOverride = null){
        def targetClass = target.class
        String simpleName = targetClass.simpleName
        String classShortName = Introspector.decapitalize(targetClass.simpleName)
        if(argsOverride == null) argsOverride = [propName, simpleName, val]
        def newCodes = [] as Set<String>
        if(!errors) errors = target.errors

        newCodes.add("${targetClass.getName()}.${propName}.${code}".toString())
        newCodes.add("${classShortName}.${propName}.${code}".toString())
        newCodes.add("${propName}.${code}".toString())
        newCodes.add(code)

        FieldError error = new FieldError(
            errors.objectName,
            errors.nestedPath + propName,
            val, //reject value
            false, //bind failure
            newCodes as String[],
            argsOverride as Object[],
            defaultMessage
        )
        def abrErrors = errors as AbstractBindingResult //this has the addError method
        abrErrors.addError(error)
    }

    String getDefaultMessage(String code) {
        try {
            if (messageSource != null) {
                return messageSource.get(code)
            }
            return ConstrainedProperty.DEFAULT_MESSAGES.get(code)
        }
        catch (e) {
            return ConstrainedProperty.DEFAULT_MESSAGES.get(code)
        }
    }
}
