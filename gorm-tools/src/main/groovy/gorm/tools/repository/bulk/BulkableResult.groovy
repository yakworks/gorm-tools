/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.bulk


import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import org.springframework.http.HttpStatus

import gorm.tools.repository.errors.api.ApiError

/*

{
  "id": 123,
  "state": "success",
  "results": [
    {
      "ok": true,
      "status": 201, //created
      "data": {
        "id": 356312,
        "num": "78987",
        "sourceId": "JOANNA75764-US123"
      }
    },
    {
      "ok": false,
      "status": 422, //validation
      "title": "Org Validation Error"
      "errors": [ { "field": "num", "message": "num can't be null" } ]
      "data": {
        "sourceId": "JOANNA75764-US123"
      },

    },
  ]
}
 */
/**
 * Bulkable result for a single entity
 * Think of this as the result of a post or put on and entity
 *


 {
    ok: false,
    requestData: {... the data that was sent }
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
    Object entityObject

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
        new BulkableResult(entityObject: entity)
    }

    static BulkableResult of(Object entity, int statusId){
        of(entity).status(statusId)
    }

    BulkableResult status(int statusId){
        this.status = HttpStatus.valueOf(statusId)
        return this
    }

    BulkableResult addTo(Results results){
        results.add(this)
        this
    }

    static class Results {
        boolean ok = true
        @Delegate List<BulkableResult> resultsList

        Results(){ resultsList = Collections.synchronizedList([]) as List<BulkableResult> }

        boolean add(BulkableResult bulkableResult){
            if(!bulkableResult.ok) ok = false
            resultsList << bulkableResult
        }

        void merge(Results results){
            if(!results.ok) ok = false
            resultsList.addAll(results.resultsList)
        }

        List<Map> transform(@ClosureParams(value = SimpleType, options = "gorm.tools.repository.bulk.BulkableResult") Closure<Map> customizer) {
            List<Map> ret = []
            boolean ok = true
            for (BulkableResult r : resultsList) {
                def map = [ok: r.ok, status: r.status.value()] as Map<String, Object>
                //do the failed
                if (!r.ok) {
                    //failed result would have original incoming map, return it as it is
                    map.putAll([
                        data: r.requestData,
                        title: r.error.title,
                        detail: r.error.detail,
                        errors: r.error.errors
                    ])
                }
                //run the customizer closure
                map.putAll customizer(r)
                ret << map
            }
            return ret
        }
    }

}
