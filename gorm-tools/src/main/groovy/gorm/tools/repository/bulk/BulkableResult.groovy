/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.bulk

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import org.springframework.http.HttpStatus

import gorm.tools.repository.errors.api.ApiError

/**
 * Bulkable result for a single entity
 * Think of this as the result of a post or put on and entity
 * {
 *     id: 123
 *     state: success
 *     results: {[
 *       {
 *         ok: true,
           entity: {
             "id": 356312,
             "num": "78987",
             "sourceId": {
               "source": {
                 "sourceId": "JOANNA75764-US123"
               }
           }
         },
 {
    ok: false,
    data: {... the data that was sent }
    error: {
        status: 422,
        title: Org Validation Error
        errors: [ array of ApiFieldError ]
    }
 }
 *     ]}
 * }
 *
 *     results: {[
 *       {
 *         ok: true,
 entity: {
 "id": 356312,
 "num": "78987",
 "org": {
 "source": {
 "sourceId": "JOANNA75764-US123"
 }
 }
 },
 {
 ok: false,
 data: {... the data that was sent }
 error: {
 status: 422,
 title: Org Validation Error
 errors: [ array of ApiFieldError ]
 }
 }
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class BulkableResult {

    boolean ok = true

    HttpStatus status

    /**
     * the entity that was created
     */
    Object entity

    /**
     * the entity fields for what was created or updated
     * if it errored then this will be null
     */
    Map entityData

    /**
     * the data the was submitted to process. will be one of the items in the list that was sent.
     */
    Map requestData

    /**
     * On error this is the processed error based on exception type
     */
    ApiError error

    static BulkableResult of(ApiError error, Map requestData){
        new BulkableResult(ok: false, error: error, requestData: requestData, status: error.status)
    }

    static BulkableResult of(Object entity){
        new BulkableResult(entity: entity)
    }

    BulkableResult status(int statusId){
        this.status = HttpStatus.valueOf(statusId)
        return this
    }
}
