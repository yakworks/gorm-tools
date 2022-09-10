/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.controller.legacy

import groovy.transform.CompileDynamic

@CompileDynamic
class ResponseHolder {
    Object data
    Map headers = [:]
    String message

    void addHeader(String name, Object value) {
        if (!headers[name]) {
            headers[name] = []
        }
        headers[name].add value?.toString()
    }
}
