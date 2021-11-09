/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api.result

import groovy.transform.CompileStatic

import gorm.tools.support.ToMessageSource

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
@CompileStatic
trait Result extends ToMessageSource {

    /**
     * the result message key or code
     */
    String code

    /**
     * A short, human-readable summary of the result type. It SHOULD NOT change from occurrence to occurrence of the
     * result, except for purposes of localization (e.g., using proactive content negotiation; see [RFC7231], Section 3.4).
     * in which case code can be used for lookup and the localization with message.properties
     */
    String title

    /**
     * status code, normally an HttpStatus.value()
     */
    Integer status

    /**
     * the response object or result of the method/function or process
     * Implementations might choose to ignore this in favor of concrete, typed fields.
     * Or this is generated from the target
     */
    Object data

    /**
     * Optional the return value or entity. Kind of like the value that Optional wraps.
     * internal in that its transient so it wont get serialized, can be used as the source to generate the data.
     */
    transient Object target

    /**
     * success or fail? if ok is true then it still may mean that there are warnings and needs to be looked into
     */
    abstract Boolean getOk()

}
