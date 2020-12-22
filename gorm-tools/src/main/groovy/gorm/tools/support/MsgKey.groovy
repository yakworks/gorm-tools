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
 * A concrete implementation of the MsgSourceResolvableTrait
 * and thus the {@link MessageSourceResolvable} interface.
 * similiar to {@link org.springframework.context.support.DefaultMessageSourceResolvable} but groovified
 * @see org.springframework.context.MessageSource#getMessage(MessageSourceResolvable, java.util.Locale)
 */
@SuppressWarnings("serial")
@CompileStatic
@EqualsAndHashCode
@ToString(includes = ['msgCodes', 'args', 'defaultMessage'], includeNames = true)
class MsgKey implements MsgSourceResolvable, Serializable {

    /**
     * Create a new empty MessageSourceKey.
     */
    MsgKey() {}

    /**
     * Create a new MessageSourceKey.
     * @param code the code to be used to resolve this message
     */
    MsgKey(String code) {
        this([code], null, null)
    }

    /**
     * Create a new MessageSourceKey.
     * @param codes the codes to be used to resolve this message
     */
    MsgKey(List<String> codes) {
        this(codes, null, null)
    }

    MsgKey(String code, String defaultMessage) {
        this([code], null, defaultMessage)
    }

    MsgKey(String code, List arguments) {
        this([code], arguments, null)
    }

    MsgKey(String code, List arguments, String defaultMessage) {
        this([code], arguments, defaultMessage)
    }

    MsgKey(Map msgMap) {
        this([msgMap.code as String], msgMap.args as List, msgMap.defaultMessage as String)
    }
    /**
     * Copy constructor: Create a new instance from another resolvable.
     * @param resolvable the resolvable to copy from
     */
    MsgKey(MessageSourceResolvable resolvable) {
        this(resolvable.codes.toList(), resolvable.arguments.toList(), resolvable.defaultMessage)
    }

    /**
     * Create a new MessageSourceKey.
     * @param codes the codes to be used to resolve this message
     * @param arguments the array of arguments to be used to resolve this message
     * @param defaultMessage the default message to be used to resolve this message
     */
    MsgKey(List<String> codes, List arguments, String defaultMessage) {
        this.msgCodes = codes
        this.args = arguments
        this.defaultMessage = defaultMessage
    }

}
