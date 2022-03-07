/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.validation

import java.beans.Introspector

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import org.grails.datastore.gorm.GormValidateable
import org.springframework.validation.AbstractBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import yakworks.commons.lang.Pogo
import yakworks.commons.lang.PropertyTools
import yakworks.i18n.MsgArgs
import yakworks.i18n.MsgKey
import yakworks.i18n.MsgMultiKey

/**
 * Reject values by adding to the errors
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@CompileStatic
class Rejector {

    GormValidateable target
    Errors errors
    // String propName
    // Object value
    // String code

    static Rejector of(GormValidateable target, Errors errors = null){
        def rv = new Rejector(target: target)
        rv.errors = errors ?: target.errors
        return rv
    }
    /**
     * validates that the prop is not null and registers error if so
     */
    static boolean validateNotNull(GormValidateable target, Errors errors, String propName){
        if (target[propName] == null) {
            Rejector.of(target, errors).withNotNullError(propName)
            return false
        }
        return true
    }

    // shortcut to reject a null value using the ConstrainedProperty defaults
    void withNotNullError(String propName) {
        withError(propName, null, [ValidationCode.NotNull.jakartaCode, ValidationCode.NotNull.name()], [:])
    }

    void withError(String propName, List<String> codes, Map args = [:], String fallbackMessage = ''){
        withError(propName, Pogo.value(target, propName), codes, args, fallbackMessage)
    }

    void withError(String propName, String code, Map args = [:], String fallbackMessage = ''){
        withError(propName, [code], args, fallbackMessage)
    }

    void withError(String propName, ValidationCode valCode, Map args = [:], String fallbackMessage = ''){
        withError(propName, Pogo.value(target, propName), [valCode.jakartaCode, valCode.name()], args, fallbackMessage)
    }

    void withError(String propName, Object val, String code, Map args = [:], String fallbackMessage = ''){
        withError(propName, val, [code], args, fallbackMessage)
    }

    void withError(String propName, Object val, List<String> codes, Map args, String fallbackMessage = ''){
        def mmk = MsgMultiKey.ofCodes(codes)
        mmk.args = MsgArgs.of(args)
        mmk.fallbackMessage = fallbackMessage
        addError(propName, val, mmk)
    }

    void addError(String propName, Object val, MsgKey msgKey){
        def targetClass = target.class
        String simpleName = targetClass.simpleName
        String classShortName = Introspector.decapitalize(targetClass.simpleName)
        // def newCodes = [] as Set<String>
        if(!errors) errors = target.errors
        Object[] args = msgKey.args.isMap() ? [msgKey.args.asMap()] as Object[] : msgKey.args.toArray()

        // newCodes.add("${targetClass.getName()}.${propName}.${code}".toString())
        // newCodes.add("${classShortName}.${propName}.${code}".toString())
        // newCodes.add("${propName}.${code}".toString())
        List newCodes
        String defaultMsg
        if(msgKey instanceof MsgMultiKey){
            newCodes = msgKey.codes
            defaultMsg = newCodes.last()
        } else {
            newCodes = [msgKey.code]
            defaultMsg = msgKey.code
        }

        FieldError error = new FieldError(
            errors.objectName,
            errors.nestedPath + propName,
            val, //reject value
            false, //bind failure
            newCodes as String[],
            args,
            defaultMsg
        )
        def abrErrors = errors as AbstractBindingResult //this has the addError method
        abrErrors.addError(error)
    }

    // static String getDefaultMessage(String code) {
    //     try {
    //         return AppCtx.msgService.get(code)
    //     }
    //     catch (e) {
    //         return ConstrainedProperty.DEFAULT_MESSAGES.get(code)
    //     }
    // }
}
