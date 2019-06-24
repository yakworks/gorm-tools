package gorm.tools.support

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TupleConstructor

import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.i18n.LocaleContextHolder

import gorm.tools.beans.AppCtx

/**
 * In many cases for parallel processing and batch processing we are spinning through chunks of data.
 * Especially when doing gpars and concurrent processing.
 * Java of course does not allow multi-value returns.
 * On errors and exceptions we don't want to stop or halt the processing. So many methods
 * can catch an exception and return this to contain basic status and a message of what went wrong so
 * we can be report on it, log it, etc and move on to try the next item.
 */
@ToString(includes = ['ok', 'id', 'message', 'meta'], includeNames = true)
@CompileStatic
@TupleConstructor(includes = ['ok', 'code', 'args', 'defaultMessage'], includeSuperFields=true)
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
