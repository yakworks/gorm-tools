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
import org.springframework.transaction.annotation.Propagation

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.job.SyncJobState
import gorm.tools.job.events.SyncJobQueueEvent
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import grails.gorm.transactions.Transactional
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.config.MaintenanceProps
import yakworks.spring.AppCtx

@Lazy @Service('syncJobService')
@CompileStatic
class DefaultSyncJobService extends SyncJobService<SyncJob> {

    //@Autowired JobProps jobProps
    @Autowired MaintenanceProps maintenanceProps

    @Autowired SyncJobRepo syncJobRepo

    @Autowired AttachmentRepo attachmentRepo

    @Autowired AttachmentSupport attachmentSupport

    @Override
    SyncJobEntity queueJob(Map data){
        MaintWindowUtil.check(maintenanceProps)
        super.queueJob(data)
    }

    @Override
    SyncJobContext createJob(SyncJobArgs args, Object payload){
        MaintWindowUtil.check(maintenanceProps)
        super.createJob(args, payload)
    }

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
