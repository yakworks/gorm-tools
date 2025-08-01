/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.json

import groovy.json.JsonGenerator
import groovy.transform.CompileStatic

import yakworks.api.problem.Violation
import yakworks.api.problem.ViolationFieldError
import yakworks.message.MsgServiceRegistry
import yakworks.message.spi.MsgService

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
        //TODO the asMap in commons should really be doing this but until we modify we do it here. 
        //if it has a msgKey and message is empty, then lookup message for code
        if(v.msg && !v.message){
            v = ViolationFieldError.of(
                v.code,
                MsgServiceRegistry.service.get(v.msg) ?: ""
            ).field(v.field)
        }
        return v.asMap()
    }

}
