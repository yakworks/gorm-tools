/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import groovy.transform.CompileStatic

//just a concrete errors implementation for the binding errors so we can use it as a placeholder
@CompileStatic
class EmptyErrors extends org.springframework.validation.AbstractBindingResult {

    EmptyErrors(String objectName) {
        super(objectName)
    }

    String getActualFieldValue(String d) {
        return getObjectName()
    }

    String getTarget() {
        return getObjectName()
    }
}
