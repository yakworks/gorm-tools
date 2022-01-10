/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import java.nio.file.Path

import groovy.transform.CompileStatic

import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.model.SourceType
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.IdGeneratorRepo
import yakworks.commons.json.JsonEngine
import yakworks.commons.map.Maps
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

            def payload = data.payload

            boolean payloadAsFile = Maps.getBoolean("payloadAsFile", be.args, false)
            if(!payloadAsFile) payloadAsFile = (payload instanceof Collection && payload.size() > 100)

            if (payload && payloadAsFile) {
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

    byte[] getData(SyncJob job){
        if(job.dataId){
            def istream = attachmentRepo.get(job.dataId).inputStream
            return IOUtils.toByteArray(istream)
        } else {
            return job.dataBytes
        }
    }

    String dataToString(SyncJob job){
        job.dataId ? attachmentRepo.get(job.dataId).getText() : getJsonString(job.dataBytes)
    }

    String payloadToString(SyncJob job){
        job.payloadId ? attachmentRepo.get(job.payloadId).getText() : getJsonString(job.payloadBytes)
    }

    String getJsonString(byte[] bytes){
        return bytes ? new String(bytes, "UTF-8") : '[]'
    }
}
