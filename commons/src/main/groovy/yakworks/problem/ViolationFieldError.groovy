/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

import yakworks.i18n.MsgKey

@ToString @EqualsAndHashCode
@TupleConstructor
@CompileStatic
class ViolationFieldError implements Violation, Serializable {
    MsgKey msg
    // message should come from msg but can be set here
    String message
    String field

    ViolationFieldError field(String v) { setField(v);  return this; }

    static ViolationFieldError of(MsgKey msg) {
        new ViolationFieldError(msg)
    }

    static ViolationFieldError of(String code, String message) {
        new ViolationFieldError(MsgKey.ofCode(code), message)
    }
}
