/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import java.nio.file.Path

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import gorm.tools.job.SyncJobService
import gorm.tools.repository.GormRepo
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentRepo

@Lazy @Service('syncJobService')
@CompileStatic
class DefaultSyncJobService implements SyncJobService {

    @Autowired
    SyncJobRepo syncJobRepo

    @Autowired
    AttachmentRepo attachmentRepo

    @Autowired
    AttachmentSupport attachmentSupport

    @Override
    GormRepo<SyncJob> getRepo(){
        return syncJobRepo
    }

    @Override
    Path createTempFile(String filename){
        return attachmentSupport.createTempFile(filename, null)
    }

    @Override
    Long createAttachment(Path sourcePath, String name) {
        Attachment attachment = attachmentRepo.create(sourcePath, name)
        return attachment.id
    }

}
