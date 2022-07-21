/*
* Copyright 2002-2018 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem

import groovy.transform.CompileStatic

import jakarta.annotation.Nullable
import yakworks.i18n.MsgKey
import yakworks.i18n.MsgKeyDecorator

/**
 * Handy class for wrapping runtime {@code Exceptions} with a root cause.
 *
 * <p>This class is {@code abstract} to force the programmer to extend
 * the class. {@code printStackTrace} and other like methods will
 * delegate to the wrapped exception, if any.
 *
 */
@CompileStatic
abstract class ProblemException<P extends ProblemTrait> extends RuntimeException implements MsgKeyDecorator{

    P problem

    protected ProblemException() {
        super();
    }

    protected ProblemException(String msg) {
        super(msg);
    }

    protected ProblemException(Throwable cause) {
        super(cause);
    }

    //implement MsgKeyDecorator, give us the code and args getters
    MsgKey getMsg() { problem.msg }
    //some basic short cut calls to problem
    String getTitle(){ problem.title }
    String getDetail(){ problem.detail }
    Object getPayload(){ problem.payload }

    /**
     * Constructs a new runtime exception with the specified detail
     * message, cause, suppression enabled or disabled, and writable
     * stack trace enabled or disabled.
     *
     * @param  message the detail message.
     * @param cause the cause.  (A {@code null} value is permitted,
     * and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression whether or not suppression is enabled
     *                          or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     *
     * @since 1.7
     */
    protected ProblemException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    ProblemException problem(P prob){
        this.problem = prob
        return this
    }

    static ProblemException of(ProblemTrait prob){
        this.newInstance().problem(prob)
    }


    /**
     * Return the detail message, including the message from the nested exception
     * if there is one.
     */
    @Override //throwable
    String getMessage() {
        return ProblemUtils.buildMessage(problem)
    }

    /**
     * Retrieve the most specific cause of this exception, that is,
     * either the innermost cause (root cause) or this exception itself.
     * <p>Differs from {@link #getRootCause()} in that it falls back
     * to the present exception if there is no root cause.
     * @return the most specific cause (never {@code null})
     */
    @Nullable
    Throwable getRootCause() {
        return ProblemUtils.getRootCause(this);
    }

    @Override
    String toString() {
        return ProblemUtils.problemToString(problem)
    }

    /**
     * Check whether this exception contains an exception of the given type:
     * either it is of the given class itself or it contains a nested cause
     * of the given type.
     * @param exType the exception type to look for
     * @return whether there is a nested exception of the specified type
     */
    public boolean contains(@Nullable Class<?> exType) {
        if (exType == null) {
            return false;
        }
        if (exType.isInstance(this)) {
            return true;
        }
        Throwable cause = getCause();
        if (cause == this) {
            return false;
        }
        if (cause instanceof ProblemException) {
            return ((ProblemException) cause).contains(exType);
        }
        else {
            while (cause != null) {
                if (exType.isInstance(cause)) {
                    return true;
                }
                if (cause.getCause() == cause) {
                    break;
                }
                cause = cause.getCause();
            }
            return false;
        }
    }

    //Override it for performance improvement, because filling in the stack trace is quit expensive
    /**
     * By default this is called in Throwable constructor
     * for performance improvement Override to disable by default.
     * to turn it back on call fillInStackTraceSuper
     */
    // @SuppressWarnings(['SynchronizedMethod'])
    // @Override
    // synchronized Throwable fillInStackTrace() { return this }
    //
    // @SuppressWarnings(['SynchronizedMethod', 'BracesForIfElse'])
    // synchronized Throwable fillInStackTraceSuper() { return super.fillInStackTrace() }

}
