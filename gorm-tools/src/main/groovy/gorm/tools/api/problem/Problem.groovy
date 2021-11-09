/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api.problem

import groovy.transform.CompileStatic

import gorm.tools.api.result.Result

/**
 * This is the base error class.
 * See https://github.com/zalando/problem fro what this is based on.
 * The ApiError models follow https://datatracker.ietf.org/doc/html/rfc7807
 * From the spec
 * A problem details object can have the following members:
 *
 * - "type" (string) - A URI reference [RFC3986] that identifies the
 *   problem type.  This specification encourages that,
 *   when dereferenced, it provide human-readable documentation for the
 *   problem type (e.g., using HTML [W3C.REC-html5-20141028]).  When
 *   this member is not present, its value is assumed to be
 *   "about:blank".
 *
 * - "title" (string) - A short, human-readable summary of the problem
 * type.  It SHOULD NOT change from occurrence to occurrence of the
 * problem, except for purposes of localization (e.g., using
 * proactive content negotiation; see [RFC7231], Section 3.4).
 *
 * - "status" (number) - The HTTP status code ([RFC7231], Section 6)
 * generated by the origin server for this occurrence of the problem.
 *
 * - "detail" (string) - A human-readable explanation specific to this
 * occurrence of the problem.

 * - "instance" (string) - A URI reference that identifies the specific
 * occurrence of the problem.  It may or may not yield further
 * information if dereferenced.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait Problem extends Result {
    String detail
    URI type = URI.create("about:blank")

    /**
     * optional, the request data, params or argument data used for the method/function or process.
     * for example in a bulk or batch processing scenario will be one of the items in the list that was sent
     * or the list slice that failed on flush/commit
     */
    Object params

    List<ProblemFieldError> errors
}
