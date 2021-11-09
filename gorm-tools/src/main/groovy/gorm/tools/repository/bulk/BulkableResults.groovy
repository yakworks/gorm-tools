/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.bulk

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import gorm.tools.api.Result

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
class BulkableResults {

    boolean ok = true
    @Delegate List<Result> resultList

    BulkableResults(boolean isSynchronized = true){
        resultList = ( isSynchronized ? Collections.synchronizedList([]) : [] ) as List<Result>
    }

    @Override //changes default list delegate so we can add ok
    boolean add(Result bulkableResult){
        if(!bulkableResult.ok) ok = false
        resultList << bulkableResult
    }

    /**
     * merges reults list int this
     * @param mergee what to merge into this one
     */
    void merge(BulkableResults mergee){
        if(!mergee.ok) ok = false
        resultList.addAll(mergee.resultList)
    }

    /**
     * transform results to list of maps, see above.
     * @param customizer closure that ruturns a map that should be merged in, runs for each item in results
     */
    List<Map> transform(List includes = null, Closure<Map> customizer = null) {
        List<Map> ret = []
        boolean ok = true
        for (Result r : resultList) {
            def map = [ok: r.ok, status: r.status] as Map<String, Object>
            //do the failed
            if (r.ok) {
                // successful result would have entity, use the includes list to prepare result object
                // def data = EntityMapService.bean.createEntityMap(r.entityObject, includes) as Map<String, Object>
                // map = [id: data['id'], data: data]
                assert r.ok
            } else {
                //failed result transforms error and saves requestData
                map.putAll([
                    data: r.requestData,
                    title: r.problem.title,
                    detail: r.problem.detail,
                    errors: r.problem.errors
                ])
            }
            //run the customizer closure
            if(customizer) {
                Map customizerResults = customizer(r as Result)
                if (customizerResults) map.putAll customizerResults
            }
            ret << map
        }
        return ret
    }

}
