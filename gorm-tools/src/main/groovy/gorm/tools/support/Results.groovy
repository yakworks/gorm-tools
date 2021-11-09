/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.support

import groovy.transform.CompileStatic
import groovy.transform.ToString

import org.springframework.context.MessageSourceResolvable

/**
 * In many cases for parallel processing and batch processing we are spinning through chunks of data.
 * Especially when doing gpars and concurrent processing.
 * Java of course does not allow multi-value returns.
 * On errors and exceptions we don't want to stop or halt the processing. So many methods
 * can catch an exception and return this to contain basic status and a message of what went wrong so
 * we can be report on it, log it, etc and move on to try the next item.
 */
@SuppressWarnings(['ConfusingMethodName', 'MethodName'])
@ToString(includes = ['ok', 'id', 'code', 'args', 'meta'], includeNames = true)
// @Builder(builderStrategy= SimpleStrategy, includes=['id', 'code'], prefix="")
//@MapConstructor(noArg=true)
@CompileStatic
class Results implements MsgSourceResolvable{
    public static final SUCCESS_CODE="results.ok"
    public static final ERROR_CODE="results.error"
    public static final FINISHED_CODE="results.finished"

    boolean ok = true

    //the optional identifier of the entity this is for
    Serializable id

    //any extra meta information that can be set and passed up the chain for an error
    Map<String, Object> meta = [:] as Map<String, Object>

    //if this is a parent Results object for a larger batch process these are the lists of succesful and failed results
    List<Results> failed = []
    List<Results> success = []

    // the error message key, may be the same as this result but sometimes we have a header
    // or title message as its called in ApiErrors and this would be for the detail
    MsgKeyError error

    // in some cases we may keep a reference to the ex so we can build errors from it later
    // perhaps for the errors
    private Exception ex

    Results(){}

    Results(String code, List args, String defaultMessage){
        setMessage(code, args, defaultMessage)
    }

    static Results of(List<Results> childList){
        new Results().fromChildList(null, childList)
    }

    static Results of(Exception ex){
        Results.error().exception(ex)
    }

    static Results error(String code, List args = null, Exception ex = null){
        Results.error().message(code, args).exception(ex)
    }

    /**
     * creates ok false with no messages. can be used like Result.error().message('foo bar')
     */
    static Results error(){
        new Results(ok: false)
    }

    static Results OK(){
        new Results()
    }

    static Results getOK(){
        return OK()
    }

    /**
     * Ok result with message code
     */
    static Results OK(String code, List args = null, String defaultMessage = null){
        new Results(code, args, defaultMessage)
    }

    /**
     * sets the default message if not setting a message code
     * allows builder syntax like Results.OK().message('some foo')
     */
    Results message(String code, List args = null, String defaultMessage = null){
        setMessage(code, args, defaultMessage)
        return this
    }

    /**
     * sets the default message if not setting a message code
     * allows builder syntax like Results.OK().defaultMessage('some foo')
     */
    Results defaultMessage(String message){
        this.defaultMessage = message
        return this
    }

    Results id(Serializable id){
        this.id = id
        if(!args) args = [id.toString()]
        return this
    }

    Results code(String code){
        this.code = code
        return this
    }

    //shortcut for default message
    Results msg(String message){
        defaultMessage(message)
    }

    /**
     * builder syntax for setting id
     */
    Results meta(Map map){
        this.meta.putAll(map)
        return this
    }

    /**
     * builder syntax for adding exception
     */
    Results exception(Exception ex){
        if(!ex) return this
        error = MsgKeyError.of(ex)
        // only fill message if not already set
        if(!code){
            this.setMessage(error)
        }
        return this
    }

    Results addFailed(Results res){
        this.ok = false
        this.failed.add(res)
        return this
    }

    Results addFailed(Exception ex){
        addFailed(Results.of(ex))
    }

    Results fromChildList(String code, List<Results> childList){
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
        return this
    }

    // public static MsgService TESTING_MSG_SERVICE //used for testing
    // //Discouraged
    // String getMessage(){
    //     return TESTING_MSG_SERVICE.get((MessageSourceResolvable)this)
    // }

}
