/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.json.converters

import groovy.json.JsonGenerator
import groovy.transform.CompileStatic

/**
 * surprisingly, there is no currency converter for json. This is it, it get registered in a META-INF/services file
 */
@CompileStatic
class CurrencyConverter implements JsonGenerator.Converter {

    @Override
    boolean handles(Class<?> type) {
        Currency == type
    }

    @Override
    Object convert(Object value, String key) {
        ((Currency)value).currencyCode
    }
}