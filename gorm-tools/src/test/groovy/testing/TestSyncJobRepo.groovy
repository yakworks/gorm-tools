/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import gorm.tools.model.SourceType
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.IdGeneratorRepo
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import yakworks.commons.json.JsonEngine

@GormRepository
@CompileStatic
class TestSyncJobRepo implements GormRepo<TestSyncJob>, IdGeneratorRepo {

    @RepoListener
    void beforeBind(TestSyncJob job, Map data, BeforeBindEvent be) {
        if (be.isBindCreate()) {
            // must be Job called from RestApi that is passing in dataPayload
            def payload = data.payload
            if (payload  && (payload instanceof Map || payload instanceof List)) {
                String res = JsonEngine.toJson(payload)
                job.payloadBytes = res.bytes
                job.sourceType = SourceType.RestApi  // we should default to RestApi if payload is passed
            }
        }
    }

    // byte[] getPayloadData(SyncJob job){
    //     if(job.payloadId){
    //         def istream = attachmentRepo.get(job.payloadId).inputStream
    //         return IOUtils.toByteArray(istream)
    //     } else {
    //         return job.payloadBytes
    //     }
    // }
    //
    // byte[] getData(SyncJob job){
    //     if(job.dataId){
    //         def istream = attachmentRepo.get(job.dataId).inputStream
    //         return IOUtils.toByteArray(istream)
    //     } else {
    //         return job.payloadBytes
    //     }
    // }
}
