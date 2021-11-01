/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.json.converters

import java.time.Instant

import groovy.json.JsonGenerator
import groovy.transform.CompileStatic

/**
 * A class to render a {@link java.time.Instant} as json
 *
 * @author James Kleeh
 */
@CompileStatic
class InstantJsonConverter implements JsonGenerator.Converter {

    @Override
    boolean handles(Class<?> type) {
        Instant == type
    }

    @Override
    Object convert(Object value, String key) {
        ((Instant)value).toEpochMilli()
    }
}
