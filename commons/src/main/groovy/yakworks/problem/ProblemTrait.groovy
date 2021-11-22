/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem

import groovy.transform.CompileStatic

import yakworks.api.ApiStatus
import yakworks.api.HttpStatus
import yakworks.api.ResultTrait
import yakworks.i18n.MsgKey

/**
 * Trait implementation for the Problem that has setters and builders
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait ProblemTrait<E extends ProblemTrait> extends ResultTrait<E> implements IProblem.Fluent<E> {
    // result overrides, always false
    Boolean getOk(){ false } //always false
    //status default to 400
    ApiStatus status = HttpStatus.BAD_REQUEST
    //this should be rendered to json if type is null
    // URI DEFAULT_TYPE = URI.create("about:blank")

    // Problem impls
    URI type //= Problem.DEFAULT_TYPE
    //the extra detail for this message
    String detail

    //if there is a cause we want to retian when we convert to exception
    Throwable cause

    // URI instance
    List<Violation> violations = [] as List<Violation> //Collections.emptyList();

    E addErrors(List<MsgKey> keyedErrors){
        def ers = getViolations()
        keyedErrors.each {
            ers << ViolationFieldError.of(it)
        }
        return (E)this
    }

    E cause(Throwable exCause){
        this.cause = exCause
        return (E)this
    }

    @Override
    String toString() {
        return ProblemUtils.problemToString(this)
    }

    ProblemException toException(){
        return getCause() ? new DefaultProblemException(getCause()).problem(this) : new DefaultProblemException().problem(this)
    }

    //static builders
    //overrides the Result/MsgKey builders
    static E create(){
        return (E)this.newInstance()
    }

    static E of(Object payload) {
        return this.newInstance().payload(payload)
    }

    static E ofCode(String code){
        return create().msg(code)
    }

    static E of(String code, Object args){
        return create().msg(code, args)
    }

    static E ofMsg(MsgKey mkey){
        return this.newInstance().msg(mkey)
    }

    static E withStatus(ApiStatus status) {
        return this.newInstance().status(status)
    }

    static E withTitle(String title) {
        return this.newInstance().title(title)
    }

    static E withDetail(String detail) {
        return this.newInstance().detail(detail)
    }

    static E ofCause(final Throwable cause) {
        def dap = this.newInstance([cause: cause])
        dap.detail(ProblemUtils.getRootCause(cause).message)
    }

}
