/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.support

import groovy.transform.CompileStatic

import org.springframework.context.MessageSourceResolvable

/**
 * Trait implementation of the {@link MessageSourceResolvable} interface.
 * similiar to {@link org.springframework.context.support.DefaultMessageSourceResolvable} but as a Trait that can
 * easily be applied to any class offers an easy way to store all the necessary values needed to resolve
 * a message via a {@link org.springframework.context.MessageSource}.
 * @see org.springframework.context.MessageSource#getMessage(MessageSourceResolvable, java.util.Locale)
 */
@CompileStatic
trait MsgSourceResolvable implements ToMessageSource, MessageSourceResolvable{

    List<String> msgCodes
    List args
    String defaultMessage

    /**
     * Sets message params from a map
     * @param msgMap a map that contains keys for code (required), args(optional) and defaultMessage(optional)
     */

    void setMessage(MessageSourceResolvable resolvable) {
        setMessage(resolvable.codes?.toList(), resolvable.arguments?.toList(), resolvable.defaultMessage)
    }

    void setMessage(String code, List arguments, String defaultMessage = null) {
        setMessage([code], arguments, defaultMessage)
    }

    void setMessage(List<String> codes, List arguments, String defaultMessage = null) {
        this.msgCodes = codes
        this.args = arguments
        this.defaultMessage = defaultMessage
    }

    /**
     * Return the default code of this resolvable, that is, the last one in the codes array.
     */
    String getCode() {
        return codes?.last()
    }

    void setCode(String cd) {
        msgCodes = [cd]
    }

    void addCode(String cd) {
        msgCodes << cd
    }

    @Override // MessageSourceResolvable
    String[] getCodes() {
        return msgCodes as String[]
    }

    @Override //MessageSourceResolvable
    Object[] getArguments() {
        return this.args as Object[]
    }

    //MessageSourceResolvable
    void setArguments(List args) {
        this.args = args
    }

    // MsgKey getMsgKey(){
    //     return MsgKey.of(getCode())
    //     [code: getCode(), args: getArgs(), defaultMessage: getDefaultMessage()]
    // }

    Map getMessageMap(){
        [code: getCode(), args: getArgs(), defaultMessage: getDefaultMessage()]
    }
}
