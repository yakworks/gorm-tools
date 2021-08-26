/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.error

import groovy.transform.CompileStatic

import org.springframework.http.HttpStatus

@CompileStatic
class ApiError {
    HttpStatus status
    String title
    String detail
}
