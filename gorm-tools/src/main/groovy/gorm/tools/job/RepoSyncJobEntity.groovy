/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import gorm.tools.model.SourceTrait
import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.PersistableRepoEntity

@CompileStatic
trait RepoSyncJobEntity<D> implements SourceTrait, PersistableRepoEntity<D, GormRepo<D>> {

    Boolean ok = false // change to TRUE if State.Finished without any issues
    SyncJobState state = SyncJobState.Running
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
