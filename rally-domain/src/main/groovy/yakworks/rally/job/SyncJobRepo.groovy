/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import java.nio.file.Path

import groovy.transform.CompileStatic

import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.job.SyncJobService
import gorm.tools.model.SourceType
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.IdGeneratorRepo
import yakworks.commons.json.JsonEngine
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentRepo

@GormRepository
@CompileStatic
class SyncJobRepo implements GormRepo<SyncJob>, IdGeneratorRepo {

    @Autowired
    AttachmentSupport attachmentSupport

    @Autowired
    AttachmentRepo attachmentRepo

    @RepoListener
    void beforeBind(SyncJob job, Map data, BeforeBindEvent be) {
        if (be.isBindCreate()) {
            // default to RestApi
            if(!data.sourceType) job.sourceType = SourceType.RestApi

            // must be Job called from RestApi that is passing in dataPayload
            def payload = data.payload
            if (payload && payload instanceof Collection && payload.size() > 1000) {
                data.payloadId =  writePayloadFile(job, data.payload)
            }
            else {
                String res = JsonEngine.toJson(payload)
                job.payloadBytes = res.bytes
            }
        }
    }

    Long writePayloadFile(SyncJob job, Object payload){
        String filename = "SyncJobPayload_${job.id}_.json"
        Path path = attachmentSupport.createTempFile(filename, null)
        JsonEngine.streamToFile(path, payload)
        Attachment attachment = attachmentRepo.create([name: filename, sourcePath: path])
        return attachment.id
    }

    byte[] getPayloadData(SyncJob job){
        if(job.payloadId){
            def istream = attachmentRepo.get(job.payloadId).inputStream
            return IOUtils.toByteArray(istream)
        } else {
            return job.payloadBytes
        }
    }

    byte[] getData(SyncJob job){
        if(job.dataId){
            def istream = attachmentRepo.get(job.dataId).inputStream
            return IOUtils.toByteArray(istream)
        } else {
            return job.dataBytes
        }
    }
}
