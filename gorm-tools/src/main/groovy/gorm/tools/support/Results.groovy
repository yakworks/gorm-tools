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
//@MapConstructor(noArg=true)
@CompileStatic
class Results implements MsgSourceResolvable{
    public static final SUCCESS_CODE="results.ok"
    public static final ERROR_CODE="results.error"
    public static final FINISHED_CODE="results.finished"

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
    // message from the exception
    String errorMessage

    Results(){}

    Results(String code, List args){
        setMessage(code, args)
    }

    Results(String code, List args, String defaultMessage){
        setMessage(code, args, defaultMessage)
    }

    static Results of(List<Results> childList){
        new Results().fromChildList(null, childList)
    }

    static Results of(Exception ex){
        Results.error(ex)
    }

    static Results error(String code, List args = null, Exception ex = null){
        Results.error().message(code, args).exception(ex)
    }

    static Results error(Exception ex){
        def res = Results.error()
        if(ex instanceof MessageSourceResolvable){
            res.setMessage(ex as MessageSourceResolvable)
        } else {
            res.defaultMessage(ex.message)
        }
        return res
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
        new Results(code, args).defaultMessage(defaultMessage)
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
    //shortcut for default message
    Results msg(String message){
        defaultMessage(message)
    }

    /**
     * builder syntax for ok
     */
    Results ok(boolean ok){
        this.ok = ok
        return this
    }

    /**
     * builder syntax for setting id
     */
    Results id(Serializable id){
        this.id = id
        return this
    }

    /**
     * builder syntax for setting code
     */
    Results code(String code){
        setCode(code)
        return this
    }

    /**
     * builder syntax for adding exception
     */
    Results exception(Exception ex){
        if(ex instanceof MessageSourceResolvable){
            this.setMessage(ex as MessageSourceResolvable)
        } else {
            this.defaultMessage(ex.message)
        }
        //FIXME dont store exception, will be huge memory killer
        this.ex = ex
        return this
    }

    //legacy
    Results addError(Results res){
        addFailed(res)
    }

    Results addFailed(Results res){
        this.ok = false
        this.failed.add(res)
        return this
    }

    Results addFailed(Exception ex){
        addFailed(Results.error(ex))
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
                msr = new MsgKey(ex['messageMap'] as Map)
            }
        }
        // return AppCtx.get("messageSource", MessageSource).getMessage(msr, LocaleContextHolder.getLocale())

        return MsgService.get(msr)
    }

}
