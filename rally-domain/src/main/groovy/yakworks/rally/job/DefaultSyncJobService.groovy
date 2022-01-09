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

@Lazy @Service('syncJobService')
@CompileStatic
class DefaultSyncJobService implements SyncJobService {

    @Autowired
    SyncJobRepo syncJobRepo

    @Autowired
    AttachmentSupport attachmentSupport

    @Override
    GormRepo<SyncJob> getJobRepo(){
        return syncJobRepo
    }

    @Override
    Path createTempFile(Serializable id){
        return attachmentSupport.createTempFile("SyncJob${id}-.json", null)
    }

}
