/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.support

import groovy.transform.CompileStatic
import groovy.transform.ToString

import org.springframework.context.MessageSourceResolvable

import gorm.tools.beans.AppCtx

/**
 * In many cases for parallel processing and batch processing we are spinning through chunks of data.
 * Especially when doing gpars and concurrent processing.
 * Java of course does not allow multi-value returns.
 * On errors and exceptions we don't want to stop or halt the processing. So many methods
 * can catch an exception and return this to contain basic status and a message of what went wrong so
 * we can be report on it, log it, etc and move on to try the next item.
 */
@ToString(includes = ['ok', 'id', 'code', 'args', 'meta'], includeNames = true)
@CompileStatic
class Results implements MsgSourceResolvableTrait{
    boolean ok = true
    //some or none of these may be filled in
    //the optional identifier of the entity this is for
    Serializable id
    //the entity that this result if for
    Object entity
    //any extra meta information that can be set and passed up the chain for an error
    Map<String, Object> meta = [:] as Map<String, Object>

    //if this is a parent Results object for a larger batch process these are the lists of succesful and failed results
    List<Results> failed = []
    List<Results> success = []

    Exception ex

    Results(){}

    Results(boolean ok, String code, List args){
        this.ok = ok
        setMessage(code, args)
    }

    Results(boolean ok, Serializable id, String code, List args = null, Exception ex = null){
        this.ok = ok
        this.id = id
        this.ex = ex
        setMessage(code, args)
    }

    Results(List<Results> childList){
        this(null, childList)
    }
    Results(String code, List<Results> childList){
        setupForLists(code, childList)
    }

    Results status(boolean ok, Map msgMap){
        this.ok = ok
        setMessage(msgMap)
    }

    static Results error(Serializable id, String code, List args = null, Exception ex = null){
        new Results(ok:false, id: id, code: code, args: args, ex: ex)
    }

    void setupForLists(String code, List<Results> childList){
        this.failed = childList.findAll { !it.ok }
        this.success = childList.findAll { it.ok }
        this.ok = failed ? false : true
        if(code){
            this.code = code
            //build args message for succes and failed count
            String argsMessage
            if(success) argsMessage = "success: ${success.size()}"
            if(failed) argsMessage = "$argsMessage , failed: ${failed.size()}"
            this.args = [argsMessage]
        } else if(failed){
            //if no code and has failed then default to filling in message from first failed
            setMessage(failed[0])
        }
    }

    String getMessage(){
        //use a var so in future we can get a bit fancier on how we contruct the message
        MessageSourceResolvable msr = this
        if(id != null && !args) args = [id.toString()]
        if(ex){
            if(ex instanceof MessageSourceResolvable){
                msr = (MessageSourceResolvable)ex
                //setMessage(ex as MessageSourceResolvable)
            }
            else if(ex.hasProperty('messageMap')){
                msr = new MessageSourceKey(ex['messageMap'] as Map)
            }
        }
        return AppCtx.get("msgService", MsgService).getMessage(msr)
    }

}
