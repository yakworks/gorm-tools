/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.json.converters

import java.time.OffsetTime
import java.time.format.DateTimeFormatter

import groovy.json.JsonGenerator
import groovy.transform.CompileStatic

/**
 * A class to render a {@link OffsetTime} as json
 *
 * @author James Kleeh
 */
@CompileStatic
class OffsetTimeJsonConverter implements JsonGenerator.Converter {

    @Override
    boolean handles(Class<?> type) {
        OffsetTime == type
    }

    @Override
    Object convert(Object value, String key) {
        DateTimeFormatter.ISO_OFFSET_TIME.format((OffsetTime)value)
    }
}
