/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.support

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

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
@TupleConstructor
@ToString(includes = ['code', 'args', 'defaultMessage'], includeNames = true)
class MsgKey implements MsgSourceResolvable, Serializable {

    /**
     * Create a new empty MessageSourceKey.
     */
    MsgKey() {}

    MsgKey(String code, List arguments = null, String defaultMessage = null) {
        this([code], arguments, defaultMessage)
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

    static MsgKey of(MessageSourceResolvable resolvable) {
        new MsgKey(resolvable.codes?.toList(), resolvable.arguments?.toList(), resolvable.defaultMessage)
    }

    static MsgKey of(String code, List arguments = null, String defaultMessage = null) {
        new MsgKey(code, arguments, defaultMessage)
    }

    static MsgKey of(String code, String defaultMessage) {
        of(code, null, defaultMessage)
    }

    static MsgKey ofDefault(String defaultMessage) {
        of(null, null, defaultMessage)
    }


}
