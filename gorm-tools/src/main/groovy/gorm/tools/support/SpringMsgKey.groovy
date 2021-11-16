/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.support

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import org.springframework.context.MessageSourceResolvable

/**
 * A concrete implementation of the MsgSourceResolvable
 * and thus the {@link MessageSourceResolvable} interface.
 * similiar to {@link org.springframework.context.support.DefaultMessageSourceResolvable} but groovified
 * @see org.springframework.context.MessageSource#getMessage(MessageSourceResolvable, java.util.Locale)
 */
@SuppressWarnings("serial")
@CompileStatic
@EqualsAndHashCode
@ToString(includes = ['code', 'args', 'defaultMessage'], includeNames = true)
class SpringMsgKey implements MsgSourceResolvable, Serializable {

    /**
     * Create a new empty MessageSourceKey.
     */
    SpringMsgKey() {}

    SpringMsgKey(String code, List arguments = null, String defaultMessage = null) {
        this([code], arguments, defaultMessage)
    }
    /**
     * Create a new MessageSourceKey.
     * @param codes the codes to be used to resolve this message
     * @param arguments the array of arguments to be used to resolve this message
     * @param defaultMessage the default message to be used to resolve this message
     */
    SpringMsgKey(List<String> codes, List arguments, String defaultMessage) {
        this.msgCodes = codes
        this.args = arguments
        this.defaultMessage = defaultMessage
    }

    static SpringMsgKey of(MessageSourceResolvable resolvable) {
        new SpringMsgKey(resolvable.codes?.toList(), resolvable.arguments?.toList(), resolvable.defaultMessage)
    }

    static SpringMsgKey of(String code, List arguments = null, String defaultMessage = null) {
        new SpringMsgKey(code, arguments, defaultMessage)
    }

    static SpringMsgKey of(String code, String defaultMessage) {
        of(code, null, defaultMessage)
    }

    static SpringMsgKey ofDefault(String defaultMessage) {
        of(null, null, defaultMessage)
    }


}
