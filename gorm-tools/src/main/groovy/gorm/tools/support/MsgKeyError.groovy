/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.support

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.transform.ToString

import org.springframework.context.MessageSourceResolvable
import org.springframework.core.NestedExceptionUtils
import org.springframework.core.NestedRuntimeException

/**
 * An error can be built from an Exception. its meant to grab the neccesary information from an exception
 * and release the exception so it can be garbage collected, as holding on to a list of
 * 1000's of exceptions would be memory hungry.
 * This will grab the msg code if the exception is a MsgSourceResolvable
 * other wise sets the defualt to the eception message.
 * Will also grab the rootCause
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.x
 */
@InheritConstructors
@ToString(includes = ['code', 'args', 'defaultMessage', 'rootCause'], includeNames = true)
@CompileStatic
class MsgKeyError extends MsgKey {

    String rootCause

    static MsgKeyError of(Exception ex){
        new MsgKeyError().exception(ex)
    }

    /**
     * does NOT build, returns either the default if it exists or just the code
     */
    String getSimpleMessage(){
        return defaultMessage ?: code
    }


    /**
     * builder syntax for adding exception
     */
    MsgKeyError exception(Exception ex){
        if(!ex) return this
        // only fill message if not already set
        if(ex instanceof MessageSourceResolvable){
            this.setMessage(ex as MessageSourceResolvable)
        } else {
            defaultMessage = ex.message
        }
        //always grab the root cause
        rootCause(ex)
        return this
    }

    MsgKeyError rootCause(Exception ex){
        if(ex instanceof NestedRuntimeException){
            rootCause = ex.rootCause ? ex.rootCause.message : ex.message
        } else {
            rootCause = NestedExceptionUtils.getRootCause(ex)?.message
        }
        return this
    }
}
