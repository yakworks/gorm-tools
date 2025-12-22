/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import groovy.transform.CompileStatic

import gorm.tools.job.SyncJobState
import gorm.tools.model.SourceType
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.LongIdGormRepo
import yakworks.api.problem.data.DataProblemCodes
import yakworks.json.groovy.JsonEngine

@GormRepository
@CompileStatic
class TestSyncJobRepo extends LongIdGormRepo<TestSyncJob> {

    @RepoListener
    void beforePersist(TestSyncJob job, BeforePersistEvent e) {
        if(!job.state) job.state = SyncJobState.Running
    }

    @RepoListener
    void beforeBind(TestSyncJob job, Map data, BeforeBindEvent be) {
        job.sourceType = SourceType.ERP
        if (be.isBindCreate()) {
            if (data.payload) {
                String res = JsonEngine.toJson(data.payload)
                job.payloadBytes = res.bytes
            }
        }
    }


}
