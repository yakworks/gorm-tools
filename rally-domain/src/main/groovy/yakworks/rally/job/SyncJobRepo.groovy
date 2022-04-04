/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import groovy.transform.CompileStatic

import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.model.SourceType
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.IdGeneratorRepo
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.repo.AttachmentRepo

@GormRepository
@CompileStatic
class SyncJobRepo implements GormRepo<SyncJob>, IdGeneratorRepo<SyncJob> {

    @Autowired
    AttachmentSupport attachmentSupport

    @Autowired
    AttachmentRepo attachmentRepo

    @RepoListener
    void beforeBind(SyncJob job, Map data, BeforeBindEvent be) {
        if (be.isBindCreate()) {
            // default to RestApi
            if(!data.sourceType) job.sourceType = SourceType.RestApi
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

    String dataToString(SyncJob job){
        job.dataId ? attachmentRepo.get(job.dataId).getText() : getJsonString(job.dataBytes)
    }

    String payloadToString(SyncJob job){
        job.payloadId ? attachmentRepo.get(job.payloadId).getText() : getJsonString(job.payloadBytes)
    }

    String errorToString(SyncJob job){
        getJsonString(job.errorBytes)
    }

    String getJsonString(byte[] bytes){
        return bytes ? new String(bytes, "UTF-8") : '[]'
    }
}
