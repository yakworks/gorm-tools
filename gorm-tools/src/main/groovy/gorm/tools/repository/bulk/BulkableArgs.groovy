/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.bulk

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import gorm.tools.repository.model.DataOp

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


@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@ToString
@CompileStatic
class BulkableArgs {


    BulkableArgs() { this([:])}

    /**
     * the operation to perform, limited to add and update right now
     */
    DataOp op

    /**
     * extra params to pass into Job, such as source and sourceId. The endpoint the
     * request came from will end up in sourceId
     */
    Map params = [:]


    /**
     * for result, list of fields to include for the created or updated entity
     */
    List<String> includes = ['id']

    /**
     * percentage of errors before it stops the job.
     * for example, if 1000 records are passed and this is set to default 10 then
     * the job will halt when it hits 100 errors
     * this setting ignored if transactional=true
     */
    int errorThreshold = 0

    /**
     * if true then the bulk operation is all or nothing, meaning 1 error and it will roll back.
     * TODO not implemented yet
     */
    boolean transactional = false

    /**
     * Allows override for the default async from gorm.tools.async.enabled
     * whether it should run async with a CompletableFuture and return the job immediately
     * or run in a standard blocking synchronous
     */
    Boolean asyncEnabled

    /**
     * Whether it should run async with a CompletableFuture and return the job immediately
     * or run in a standard blocking synchronous
     */
    Boolean promiseEnabled = false

    /**
     * the args, such as flush:true etc.., to pass down to the repo methods
     */
    Map persistArgs = [:]

    static BulkableArgs of(DataOp dataOp){
        new BulkableArgs(op: dataOp)
    }

    static BulkableArgs create(Map args = [:]){
        args.op = DataOp.add
        new BulkableArgs(args)
    }

    static BulkableArgs update(Map args = [:]){
        args.op = DataOp.update
        new BulkableArgs(args)
    }
}
