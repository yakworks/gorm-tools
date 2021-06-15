/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common

import groovy.transform.CompileStatic

@SuppressWarnings(['MethodName'])
@CompileStatic
trait NameCodeDescription extends NameDescription {

    String code

    static List qSearchIncludes = ['name', 'code', 'description'] // quick search includes
    static List picklistIncludes = ['id', 'code'] //for picklist

    static constraintsMap = [
        code:[ description: 'Short code, alphanumeric with no special characters except dash (for space) and underscore', nullable: false,
               blank: false, maxSize: 10, matches: "[a-zA-Z0-9-_]+"]
    ]

    void beforeValidate() {
        if(!this.name && this.code) this.name = code.replaceAll('-',' ')
    }
}
