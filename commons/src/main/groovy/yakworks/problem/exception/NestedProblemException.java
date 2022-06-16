/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yakworks.problem.exception;

import yakworks.problem.ProblemUtils;

import jakarta.annotation.Nullable;

/**
 * Handy class for wrapping runtime {@code Exceptions} with a root cause.
 *
 * <p>This class is {@code abstract} to force the programmer to extend
 * the class. {@code printStackTrace} and other like methods will
 * delegate to the wrapped exception, if any.
 *
 * Base on springsource
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getMessage
 */
public abstract class NestedProblemException extends RuntimeException {

    public NestedProblemException() {
        super();
    }

	/**
	 * Construct a {@code NestedRuntimeException} with the specified detail message.
	 * @param msg the detail message
	 */
	public NestedProblemException(String msg) {
		super(msg);
	}

    public NestedProblemException(Throwable cause) {
        super(cause);
    }

	/**
	 * Construct a {@code NestedRuntimeException} with the specified detail message
	 * and nested exception.
	 * @param msg the detail message
	 * @param cause the nested exception
	 */
	public NestedProblemException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

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
    protected NestedProblemException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

	/**
	 * Return the detail message, including the message from the nested exception
	 * if there is one.
	 */
    @Override //throwable
    public String getMessage() {
        return ProblemUtils.buildMessage(this);
    }

	/**
	 * Retrieve the innermost cause of this exception, if any.
	 * @return the innermost exception, or {@code null} if none
	 * @since 2.0
	 */
	@Nullable
	public Throwable getRootCause() {
		return NestedExceptionUtils.getRootCause(this);
	}

	/**
	 * Retrieve the most specific cause of this exception, that is,
	 * either the innermost cause (root cause) or this exception itself.
	 * <p>Differs from {@link #getRootCause()} in that it falls back
	 * to the present exception if there is no root cause.
	 * @return the most specific cause (never {@code null})
	 * @since 2.0.3
	 */
	public Throwable getMostSpecificCause() {
		Throwable rootCause = getRootCause();
		return (rootCause != null ? rootCause : this);
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
		if (cause instanceof NestedProblemException) {
			return ((NestedProblemException) cause).contains(exType);
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
