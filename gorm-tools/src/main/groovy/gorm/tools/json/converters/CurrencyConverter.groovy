/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.json.converters

import groovy.transform.CompileStatic

import grails.plugin.json.builder.JsonGenerator

/**
 * Currency converter for json-views. registered in a META-INF/services file
 */
@CompileStatic
class CurrencyConverter implements JsonGenerator.Converter {

    @Override
    boolean handles(Class<?> type) {
        Currency.isAssignableFrom(type)
    }

    @Override
    Object convert(Object value, String key) {
        ((Currency)value).currencyCode
    }
}
