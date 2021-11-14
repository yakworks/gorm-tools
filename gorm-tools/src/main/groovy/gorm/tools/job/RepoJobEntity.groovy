/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import gorm.tools.model.SourceTrait
import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.PersistableRepoEntity

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

@CompileStatic
trait RepoJobEntity<D> implements SourceTrait, PersistableRepoEntity<D, GormRepo<D>> {

    Boolean ok = false // change to TRUE if State.Finished without any issues
    JobState state = JobState.Running
    // data we are getting. For RestApi calls it's data body
    byte[] requestData

    // String fileWithJson  // option if json is too big

    //The "data" is a response of resources that were successfully and unsuccessfully updated or created after processing.
    // The data differ depending on the sourceType of the job
    byte[] data

    /**
     * returns the data byte array as a raw json string.
     * If no data then returns string repreentation of json empty array which is '[]'
     */
    String dataToString(){
        return getData() ? new String(getData(), "UTF-8") : '[]'
    }

    String requestDataToString(){
        return getRequestData() ? new String(getRequestData(), "UTF-8") : '[]'
    }
}
