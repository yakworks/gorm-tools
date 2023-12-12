/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.log

import org.springframework.context.ApplicationEvent

class InvalidApiCallEvent extends ApplicationEvent {

    String code
    String title
    String detail
    String user
    Object payload

    InvalidApiCallEvent(String code, String title, String detail, String user = null, Object payload = null) {
        super(code)
        this.code = code
        this.title = title
        this.detail = detail
        this.user = user
        this.payload = payload
    }
}
