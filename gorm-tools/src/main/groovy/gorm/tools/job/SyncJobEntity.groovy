/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import gorm.tools.model.SourceTrait

/*
transform example when in a job
{
  "id": 123, // jobId
  "status": 200, //201 created?, do we send back a 400 if its ok:false? also a 207 Multi-Status options maybe?
  "ok": false
  "state": "finished", //from job
  "errors": [
     {
        "title": "ZZZ constraint violation",
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
trait SyncJobEntity<D> implements SourceTrait {

    public static int MAX_MEG_IN_BYTES = 1024 * 1024 * 10 //10 megabytes

    /**
     * will be true if State.Finished without any issues, false if any problems ar in the results data
     */
    Boolean ok = false // change to TRUE if State.Finished without any issues

    /**
     * the current state of the job.
     */
    SyncJobState state = SyncJobState.Running

    String message

    /**
     * if payload is stored as an attachment then this will be the id
     */
    Long payloadId

    /**
     * if the payload data is stored in the column this will be populated
     */
    byte[] payloadBytes

    // /**
    //  * gets the payloadData as byte array, either from attachment file or payloadBytes byte array
    //  */
    // abstract byte[] getPayloadData()

    /**
     * if payload is stored as an attachment then this will be the id
     */
    Long dataId

    // abstract byte[] getData()

    /**
     * if the resultData is stored in the column this will be populated
     */
    byte[] dataBytes

    /**
     * if the errors are stored in the column this will be populated
     */
    //byte[] problemsBytes

    List problems

    /**
     * The data is a response of resources that were successfully and unsuccessfully updated or created after processing.
     * gets the data as byte array, either from attachment file or resultData byte array
     * If no data then returns string representation of json empty array which is '[]'
     */
    String dataToString() {
        return dataBytes ? new String(dataBytes, "UTF-8") : '[]'
    }

    String payloadToString() {
        return payloadBytes ? new String(payloadBytes, "UTF-8") : '[]'
    }

    // String problemsToString() {
    //     return problemsBytes ? new String(problemsBytes, "UTF-8") : '[]'
    // }

    static constraintsMap = [
        state       : [d: 'State of the job', nullable: false],
        message     : [d: 'Status message or log', maxSize: 500],
        payloadId   : [d: 'If payload is stored as attahcment file this is the id', oapi: "NO"],
        payloadBytes: [d      : 'Json payload data (stored as byte array) that is passed in, for example list of items to bulk create',
                       maxSize: MAX_MEG_IN_BYTES, oapi: "NO"],
        dataId      : [d: 'If data is saved as attahchment file this is the id', oapi: "NO"],
        dataBytes   : [d: 'The result data stored as bytes', maxSize: MAX_MEG_IN_BYTES, oapi: "NO"],
        //errorBytes  : [d: 'The error data stored as bytes', maxSize: MAX_MEG_IN_BYTES, oapi: "NO"],
    ]
}
