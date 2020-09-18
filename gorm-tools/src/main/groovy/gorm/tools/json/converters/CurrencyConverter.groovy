package gorm.tools.json.converters

import groovy.transform.CompileStatic

import grails.plugin.json.builder.JsonGenerator

/**
 * surprisingly, there is no currency converter for json. This is it, it get registered in a META-INF/services file
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
