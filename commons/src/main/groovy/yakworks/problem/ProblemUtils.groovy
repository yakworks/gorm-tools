/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.problem


import groovy.transform.CompileStatic

import yakworks.api.ResultUtils

@CompileStatic
class ProblemUtils {

    static String problemToString(final IProblem p) {
        String concat = ResultUtils.resultToStringCommon(p)
        String type = p.type ? "type=$p.type" : null
        concat = [concat, type, p.detail].findAll{it != null}.join(', ')
        String probName = p.class.simpleName
        return "${probName}(${concat})"
    }

    static String buildMessage(final Object problem) {
        IProblem p = (IProblem)problem
        String code = p.code ? "code=$p.code" : ''
        return [p.title, p.detail, code].findAll{it}.join(': ')
    }

    /**
     * Retrieve the innermost cause of the given exception
     * Returns the original passed in exception if there is no root cause
     * so this alway returns something. to check if it hsa a root cause then
     * can just do getRootCause(ex) == ex
     */
    static Throwable getRootCause(Throwable original) {
        if (original == null) {
            return null
        }
        Throwable rootCause = null
        Throwable cause = original.getCause()
        while (cause != null && cause != rootCause) {
            rootCause = cause;
            cause = cause.getCause()
        }
        return (rootCause != null ? rootCause : original)
    }

}
