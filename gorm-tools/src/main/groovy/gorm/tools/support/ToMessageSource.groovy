/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.support

import groovy.transform.CompileStatic

import org.springframework.context.MessageSourceResolvable

/**
 * Trait that that say an object can be a MessageSource
 * Think of it like ToString but for messages.properties
 */
@CompileStatic
trait ToMessageSource {

    MessageSourceResolvable toMessageSource() {
        if(MessageSourceResolvable.isAssignableFrom(this.class)){
            return SpringMsgKey.of(this as MessageSourceResolvable)
        }
        //pull it from the keys
        Map props = this.properties
        if(props.code) {
            def args = props.msgArgs?:props.arguments
            return SpringMsgKey.of(props.code as String, args as List, props.defaultMessage as String)
        } else if(props.defaultMessage) {
            return SpringMsgKey.ofDefault(props.defaultMessage as String)
        }
    }

}
