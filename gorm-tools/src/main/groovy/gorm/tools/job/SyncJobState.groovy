/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import gorm.tools.model.IdEnum

@CompileStatic
enum SyncJobState implements IdEnum<SyncJobState, Integer> {
    Queued(0),
    Running(1),
    Finished(2), // completed succesfully or with errors
    Cancelled(3), // killed, stopped
    WTF(4)  // WORK THAT FAILED -- was running or queued and we had to change state to something becasue it's not running

    Integer id

    SyncJobState(Integer id) {
        this.id = id
    }
}
