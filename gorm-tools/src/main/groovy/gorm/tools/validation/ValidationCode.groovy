/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.validation

import groovy.transform.CompileStatic

import yakworks.commons.lang.EnumUtils

/**
 * Adapts the Gorm error codes to jakarta validation codes
 */
@CompileStatic
enum ValidationCode {

    CreditCardNumber,
    Email,
    NotBlank,
    Range,
    InList,
    URL,
    Pattern,
    Size,
    Min,
    Max,
    MaxLength,
    MinLength,
    Scale,
    NotEqual,
    NotNull

    //case insensitive find
    static ValidationCode get(Object key){
        if(!key) return null
        return EnumUtils.getEnum(ValidationCode, key.toString().toLowerCase())
    }
}
