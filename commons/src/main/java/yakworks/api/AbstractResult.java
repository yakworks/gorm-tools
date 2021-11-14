/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.api;

import yakworks.i18n.MsgKey;

import java.io.Serializable;
import java.util.Map;

/**
 * Simple OkResult with a Map as the data object
 *
 * @author Joshua Burnett (@basejump)
 */
public class AbstractResult<E> implements Result<E>, Serializable {

    private ApiStatus status = HttpStatus.OK;
    @Override public ApiStatus getStatus() {
        return status;
    }
    @Override public void setStatus(ApiStatus v) {this.status = v;}

    private MsgKey msg;
    @Override public MsgKey getMsg() {
        return msg;
    }
    @Override public void setMsg(MsgKey v) {this.msg = v;}

    private String title;
    @Override public String getTitle() { return title;}
    @Override public void setTitle(String title) { this.title = title;}

    private Object data;
    @Override public Object getData() { return data;}
    @Override public void setData(Object v) {this.data = v;}

    // private T value;
    // @Override public T getValue() { return value;}
    // @Override public void setValue(T v) {this.value = v;}

}
