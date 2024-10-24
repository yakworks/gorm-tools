/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.json

import groovy.json.JsonGenerator
import groovy.transform.CompileStatic

import yakworks.api.problem.Violation

/**
 * Groovy json converter for Violation
 */
@CompileStatic
class ViolationConverter implements JsonGenerator.Converter {

    @Override
    boolean handles(Class<?> type) {
        Violation.isAssignableFrom(type)
    }

    @Override
    Object convert(Object value, String key) {
        def v = (Violation)value
        return v.asMap()
    }

}
