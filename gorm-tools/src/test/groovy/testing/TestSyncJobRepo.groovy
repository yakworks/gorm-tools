/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import gorm.tools.job.SyncJobService
import gorm.tools.model.SourceType
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.IdGeneratorRepo
import groovy.transform.CompileStatic
import yakworks.commons.json.JsonEngine

@GormRepository
@CompileStatic
class TestSyncJobRepo implements GormRepo<TestSyncJob>, IdGeneratorRepo {

    @RepoListener
    void beforeBind(TestSyncJob job, Map data, BeforeBindEvent be) {
        generateId(job)
        job.sourceType = SourceType.RestApi
        if (be.isBindCreate()) {
            if (data.payload) {
                String res = JsonEngine.toJson(data.payload)
                job.payloadBytes = res.bytes
            }
        }
    }

}
