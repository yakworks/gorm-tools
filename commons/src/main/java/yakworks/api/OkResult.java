/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api;

import yakworks.i18n.MsgKey;
import java.io.Serializable;
import java.util.Map;

/**
 * Used as a result object in api for any api that can be ok or may have a problem
 *
 * @param <D> Data, will normally be a Map or a List but can be anything
 * @author Joshua Burnett (@basejump)
 */
public class OkResult<D> implements Result<D>, Serializable {

    private ApiStatus status = HttpStatus.OK;
    private MsgKey msgKey;
    private String title;

    private D data;
    private Object target;

    @Override
    public String getCode() { return msgKey != null ? msgKey.getCode() : null;}

    @Override
    public ApiStatus getStatus() { return status;}

    @Override
    public MsgKey getMsgKey() { return msgKey;}

    @Override
    public String getTitle() { return title;}

    @Override
    public D getData() { return data;}

    @Override
    public Object getTarget() { return target;}

    // // Builders
    OkResult<D> title(String v) { title = v;  return this; }
    OkResult<D> data(D v)       { data = v;   return this; }
    OkResult<D> target(Object v){ target = v; return this; }

    OkResult<D> msgKey(MsgKey v){ msgKey = v; return this; }
    OkResult<D> msgKey(String v, Map args) {
        msgKey = MsgKey.of(v, args);
        return this;
    }

    OkResult<D> status(ApiStatus v) { status = v; return this; }
    OkResult<D> status(Integer v){ status = HttpStatus.valueOf(v); return this; }

}
