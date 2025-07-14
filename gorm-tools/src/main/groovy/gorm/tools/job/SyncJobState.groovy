/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

@CompileStatic
enum SyncJobState {
    Queued,
    Running,
    Finished, // completed succesfully or with errors
    Cancelled, // killed, stopped
    WTF  // WORK THAT FAILED -- was running or queued and we had to change state to something becasue it's not running

    boolean isComplete(){
        this == Finished || this == Cancelled || this == WTF
    }

    boolean isFinished(){
        this == Finished
    }
}
