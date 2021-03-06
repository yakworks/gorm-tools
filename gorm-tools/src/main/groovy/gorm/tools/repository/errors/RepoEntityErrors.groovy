/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import java.beans.Introspector

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormValidateable
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.AbstractBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import gorm.tools.beans.AppCtx
import grails.gorm.validation.ConstrainedProperty

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

    MessageSource getMessageSource(){
        AppCtx.getCtx()
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
    void rejectValueWithMessage(GormValidateable target, Errors errors, String propName, Object val, String code,
                                String defaultMessage = null, Object argsOverride = null){
        def targetClass = target.class
        if(argsOverride == null) argsOverride = [propName, targetClass, val]
        def newCodes = [] as Set<String>
        if(!errors) errors = target.errors
        String classShortName = Introspector.decapitalize(targetClass.getSimpleName())
        newCodes.add("${targetClass.getName()}.${propName}.${code}".toString())
        newCodes.add("${classShortName}.${propName}.${code}".toString())
        newCodes.add("${code}.${propName}".toString())
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
            if (getMessageSource() != null) {
                return getMessageSource().getMessage(code, null, LocaleContextHolder.getLocale())
            }
            return ConstrainedProperty.DEFAULT_MESSAGES.get(code)
        }
        catch (e) {
            return ConstrainedProperty.DEFAULT_MESSAGES.get(code)
        }
    }
}
