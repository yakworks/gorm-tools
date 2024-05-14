/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.log

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import org.springframework.context.ApplicationEvent

@CompileStatic
@Builder(builderStrategy= SimpleStrategy, prefix="")
class InvalidApiCallEvent extends ApplicationEvent {

    String uri
    String code
    String title
    String detail
    String user
    Object payload

    InvalidApiCallEvent(String uri) {
        super(uri)
        this.uri = uri
    }

    static InvalidApiCallEvent of(String uri, String code) {
        return new InvalidApiCallEvent(uri).code(code)
    }
}
