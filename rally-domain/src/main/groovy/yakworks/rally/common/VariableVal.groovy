/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@SuppressWarnings(['MethodName'])
@CompileStatic
trait VariableVal {

    String variable
    String value

    static List qSearchIncludes = ['variable', 'value'] // quick search includes

    static constraintsMap = [
        variable:[ description: 'Parameter config prop name, unique', nullable: false, blank: false, maxSize: 100],
        value:[ description: 'The value', nullable: false, blank: false, maxSize: 50]
    ]

}
