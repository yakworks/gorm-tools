/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.databinding

import groovy.transform.CompileStatic

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError

import grails.databinding.errors.BindingError
import grails.databinding.events.DataBindingListenerAdapter
import grails.util.GrailsNameUtils

/**
 * Copy of GrailsWebDataBindingListener so we dont need to depend on the grails web-databinder that messes with the domains because
 * of the WebDataBindingTraitInjector that messes up the map constructor
 * This is really just used for the bindingError method to update the erros
 */
@CompileStatic
class EntityMapBindingListener extends DataBindingListenerAdapter {
    private final MessageSource messageSource

    EntityMapBindingListener(MessageSource messageSource) {
        this.messageSource = messageSource
    }

    @Override
    void bindingError(BindingError error, Object errors) {
        BindingResult bindingResult = (BindingResult)errors
        String className = error.object?.getClass()?.getName()
        String classAsPropertyName = GrailsNameUtils.getPropertyNameRepresentation(className)
        String propertyName = error.getPropertyName()
        String[] codes = [
            className + '.' + propertyName + '.typeMismatch.error',
            className + '.' + propertyName + '.typeMismatch',
            classAsPropertyName + '.' + propertyName + '.typeMismatch.error',
            classAsPropertyName + '.' + propertyName + '.typeMismatch',
            bindingResult.resolveMessageCodes('typeMismatch', propertyName),
        ].flatten() as String[]
        Object[] args = [getPropertyName(className, classAsPropertyName, propertyName)] as Object[]
        def defaultMessage = error.cause?.message ?: 'Data Binding Failed'
        def fieldError = new FieldError(className, propertyName, error.getRejectedValue(), true, codes, args, defaultMessage)
        bindingResult.addError(fieldError)
    }

    protected String getPropertyName(String className, String classAsPropertyName, String propertyName) {
        if (!messageSource) return propertyName

        final Locale locale = LocaleContextHolder.getLocale()
        String propertyNameCode = className + '.' + propertyName + ".label"
        String resolvedPropertyName = messageSource.getMessage(propertyNameCode, null, propertyName, locale)
        if (resolvedPropertyName == propertyName) {
            propertyNameCode = classAsPropertyName + '.' + propertyName + ".label"
            resolvedPropertyName = messageSource.getMessage(propertyNameCode, null, propertyName, locale)
        }
        return resolvedPropertyName
    }
}
