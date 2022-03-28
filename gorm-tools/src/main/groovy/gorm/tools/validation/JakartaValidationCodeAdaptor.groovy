/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.validation

import groovy.transform.CompileStatic

import grails.gorm.validation.ConstrainedProperty

/**
 * Adapts the Gorm error codes to jakarta validation codes
 */
@CompileStatic
class JakartaValidationCodeAdaptor {

    static Map codeMap = [
        'creditCard.invalid': ValidationCode.CreditCardNumber,
        'email.invalid'    : ValidationCode.Email,
        'blank'      : ValidationCode.NotBlank,
        'range.toobig'      : ValidationCode.Range,
        'range.toosmall'      : ValidationCode.Range,
        'not.inList'    : ValidationCode.InList,
        'url.'        : ValidationCode.URL,
        'matches.invalid'    : ValidationCode.Pattern,
        // (ConstrainedProperty.SIZE_CONSTRAINT)       : ValidationCode.Size,
        'min.notmet'      : ValidationCode.Min,
        'max.exceeded'       : ValidationCode.Max,
        'maxSize.exceeded'   : ValidationCode.MaxLength,
        'minSize.notmet'   : ValidationCode.MinLength,
        'size.toobig'   : ValidationCode.MaxLength,
        'size.toosmall'   : ValidationCode.MinLength,
        'scale'      : ValidationCode.Scale,
        'notEqual'  : ValidationCode.NotEqual,
        'nullable'   : ValidationCode.NotNull
    ]

    static Map messagesMap = [
        (ValidationCode.CreditCardNumber): [
            code: 'org.hibernate.validator.constraints.CreditCardNumber.message',
            defaultMessage: 'invalid credit card number'
        ],
        (ValidationCode.Email): [
            code: 'jakarta.validation.constraints.Email.message',
            defaultMessage: 'must be a well-formed email address'
        ],
        (ValidationCode.NotBlank): [
            code: 'jakarta.validation.constraints.NotBlank.message',
            defaultMessage: 'must not be empty'
        ],
        (ValidationCode.Range): [
            code: 'org.hibernate.validator.constraints.Range.message',
            defaultMessage:'must be between {min} and {max}'
        ],
        (ValidationCode.URL): [
            code: 'org.hibernate.validator.constraints.URL.message',
            defaultMessage:'must be a valid URL'
        ],
        (ValidationCode.Pattern) : [
            code: 'jakarta.validation.constraints.Pattern.message',
            defaultMessage: 'must match "{regexp}"'
        ],
        (ValidationCode.Size): [
            code: 'jakarta.validation.constraints.Size.message',
            defaultMessage: 'size must be between {min} and {max}'
        ],
        (ValidationCode.Min): [
            code: 'jakarta.validation.constraints.Min.message',
            defaultMessage: 'must be less than or equal to {value}'
        ],
        (ValidationCode.Max) : [
            code: 'jakarta.validation.constraints.Max.message',
            defaultMessage: 'must be greater than or equal to {value}'
        ],
        (ValidationCode.NotNull): [
            code: 'jakarta.validation.constraints.NotNull.message',
            defaultMessage:'must not be null'
        ],

        // no ValidationMessages for these

        (ValidationCode.MaxLength): [
            code: 'validation.constraints.MaxLength',
            defaultMessage: 'Length must be greater than or equal to {value}'
        ],
        (ValidationCode.MinLength): [
            code: 'validation.constraints.MinLength',
            defaultMessage: 'must be less than or equal to {value}'
        ],

        (ValidationCode.InList): [
            code: 'validation.constraints.InList',
            defaultMessage: 'must be in list [{value}]'
        ],
        (ValidationCode.NotEqual): [
            code: 'validation.constraints.NotEqual.message',
            defaultMessage:'must not equal {value}'
        ],
    ]
}
