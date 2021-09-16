/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.bulk

class BulkableArgs {
    /**
     * what to set the job.source to
     */
    String jobSource

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

    // if true then the whole set should be in a transaction. disables parallelProcessing.
    boolean transactional = false

    // whether it should return thr job imediately or do it sync
    boolean async = true

}
