/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api;

import yakworks.i18n.MsgKey;
import yakworks.i18n.MsgKeyDecorator;

/**
 * This is the base result trait for problems and results
 * follows https://datatracker.ietf.org/doc/html/rfc7807 for status and title fields
 *
 * In many cases for parallel processing and batch processing we are spinning through chunks of data.
 * Especially when doing gpars and concurrent processing.
 * Java of course does not allow multi-value returns.
 * On errors and exceptions we don't want to stop or halt the processing. So many methods
 * can catch an exception and return this to contain basic status and a message of what went wrong so
 * we can be report on it, log it, etc and move on to try the next item.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
public interface Result extends MsgKeyDecorator {

    default String getDefaultCode() {
        return null;
    }

    /**
     * A short, human-readable summary of the result type. It SHOULD NOT change from occurrence to occurrence of the
     * result, except for purposes of localization (e.g., using proactive content negotiation; see [RFC7231], Section 3.4).
     * in which case code can be used for lookup and the localization with message.properties
     */
    default String getTitle() {
        return null;
    }
    default void setTitle(String title){}

    /**
     * status code, normally an HttpStatus.value()
     */
    default ApiStatus getStatus() {
        return HttpStatus.OK;
    }
    default void setStatus(ApiStatus v){}

    /**
     * the response object value or result of the method/function or process
     * Implementations might choose to ignore this in favor of concrete, typed fields.
     * Or this is generated from the target
     */
    default Object getPayload() { return null; }
    default void setPayload(Object v){}

    /**
     * alias to payload
     */
    default Object getValue(){ return getPayload(); }

    /**
     * Optional the return value or entity. Kind of like the value that Optional wraps.
     * internal in that its transient so it wont get serialized, can be used as the source to generate the data.
     */
    // default T getValue() { return null; }
    // default void setValue(T v){}
    // default E value(T v){ setValue(v); return (E)this; }

    /**
     * success or fail? if ok is true then it still may mean that there are warnings and needs to be looked into
     */
    default Boolean getOk() {
        return true;
    }

    /**
     * get the value of the payload, keeps api similiar to Optional.
     */
    default Object get(){ return getPayload(); }

    //STATIC HELPERS

    static OkResult OK() {
        return new OkResult();
    }

    static OkResult ofCode(String code) {
        return of(code, null);
    }

    static OkResult of(String code, Object args) {
        return new OkResult(MsgKey.of(code, args));
    }

    /**
     * java.util.Optional api consitency. Creates a result with the value as the payload
     */
    static OkResult of(Object value) {
        return new OkResult(value);
    }


    interface Fluent<E extends Fluent> extends Result {
        default E title(String v) { setTitle(v);  return (E)this; }
        default E status(ApiStatus v) { setStatus(v); return (E)this; }
        default E status(Integer v) { setStatus(HttpStatus.valueOf(v)); return (E)this; }
        default E payload(Object v) { setPayload(v); return (E)this; }
        //aliases to payload
        default E value(Object v) { return payload(v); }

        default E msg(MsgKey v){ setMsg(v); return (E)this; }
        default E msg(String v) {
            if(getMsg() == null){
                return msg(MsgKey.ofCode(v));
            } else {
                getMsg().setCode(v);
                return (E)this;
            }
        }
        default E msg(String v, Object args) { return msg(MsgKey.of(v, args));}
    }
}
