/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem.exception

import groovy.transform.CompileStatic

import jakarta.annotation.Nullable
import yakworks.api.ApiStatus
import yakworks.i18n.MsgKey
import yakworks.problem.IProblem
import yakworks.problem.Problem

@CompileStatic
final class ProblemBuilder<T extends IProblem> {

    private URI type
    private String title
    private ApiStatus status
    private String detail
    // private URI instance
    private Throwable cause
    private MsgKey msg
    private Object payload
    private Class<T> problemClass

    ProblemBuilder() {
        this.problemClass = Problem as Class<T>
    }

    ProblemBuilder(Class<T> problemType) {
        this.problemClass = problemType
    }

    ProblemBuilder<T> payload(Object v) {
        this.payload = v;
        return this;
    }

    ProblemBuilder<T> msg(MsgKey msg) {
        this.msg = msg;
        return this;
    }

    ProblemBuilder<T> type(@Nullable final URI type) {
        this.type = type;
        return this;
    }

    public ProblemBuilder<T> title(@Nullable final String title) {
        this.title = title;
        return this;
    }

    public ProblemBuilder<T> status(@Nullable final ApiStatus status) {
        this.status = status;
        // if(this.title == null) this.title = status.getReason();
        return this;
    }

    public ProblemBuilder<T> detail(@Nullable final String detail) {
        this.detail = detail;
        return this;
    }

    // public ProblemBuilder<T> instance(@Nullable final URI instance) {
    //     this.instance = instance;
    //     return this;
    // }

    public ProblemBuilder<T> cause(@Nullable final Throwable cause) {
        this.cause = cause;
        return this;
    }

    T build() {
        T rp = cause ? problemClass.newInstance(cause) : problemClass.newInstance()

        //ugly but getting it working for now
        if (payload) rp.payload = payload
        if (msg) rp['msg'] = msg //not sure why we are getting 'Cannot set read-only property'
        if (type) rp.type = type
        if (title) rp.title = title
        if (status) rp.status = status
        if (detail) rp.detail = detail
        // if(instance) rp.instance = instance
        return rp
    }

    static ProblemBuilder of(Class problemType) {
        return new ProblemBuilder(problemType);
    }

}
