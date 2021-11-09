/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import gorm.tools.api.problem.Problem
import gorm.tools.api.result.Result

/*
transform example when in a job
{
  "id": 123, // jobId
  "status": 200, //201 created?, do we send back a 400 if its ok:false? also a 207 Multi-Status options maybe?
  "ok": false
  "state": "finished", //from job
  "errors": [
     {
        "title": "XXX constraint violation",
        "detail" "Data Access Exception"
        }
   ],
  "data": [
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
        "sourceId": "JOANNA75764-US123" ...
     },

    },
  ]
}
 */

/**
 * Bulkable result for a single entity
 * Think of this as the result of a post or put on and entity
 *
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class ApiResults implements Result {
    //reimplement Result fields so @Builder will see them, easier to do this than to manually implement builder methods
    String code; Integer status = 207; String title; Object params;

    Boolean ok = true

    @Delegate List<Result> resultList

    ApiResults(boolean isSynchronized = true){
        resultList = ( isSynchronized ? Collections.synchronizedList([]) : [] ) as List<Result>
    }

    static ApiResults OK(){
        new ApiResults()
    }

    @Override //changes default list delegate so we can add ok
    boolean add(Result result){
        if(!result.ok) ok = false
        resultList << result
    }

    /**
     * if resultToMerge is ApiResults then add all from its resultList
     * else just call add to list
     */
    void merge(Result resultToMerge){
        if(!resultToMerge.ok) ok = false
        if(resultToMerge instanceof ApiResults){
            resultList.addAll(resultToMerge.resultList)
        } else {
            resultList << resultToMerge
        }

    }

    /**
     * returns the problems
     */
    List<Problem> getProblems(){
        //only look if this is not ok as it should never have problems if ok=true
        if(!this.ok){
            resultList.find{ it instanceof Problem } as List<Problem>
        } else {
            [] as List<Problem>
        }

    }

    /**
     * returns the successful results
     */
    List<Result> getResults(){
        resultList.find{ it.ok } as List<Result>
    }

}
