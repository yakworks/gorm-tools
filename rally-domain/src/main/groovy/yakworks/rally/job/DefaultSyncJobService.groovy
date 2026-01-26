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

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.job.SyncJobState
import gorm.tools.repository.GormRepo
import yakworks.json.groovy.JsonEngine
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.config.MaintenanceProps

@Lazy @Service('syncJobService')
@CompileStatic
class DefaultSyncJobService extends SyncJobService<SyncJob> {

    //@Autowired JobProps jobProps
    @Autowired MaintenanceProps maintenanceProps

    @Autowired SyncJobRepo syncJobRepo

    @Autowired AttachmentRepo attachmentRepo

    @Autowired AttachmentSupport attachmentSupport

    // @Override
    // SyncJobEntity queueJob(Map data){
    //     MaintWindowUtil.check(maintenanceProps)
    //     super.queueJob(data)
    // }

    @Override
    SyncJobEntity queueJob(SyncJobArgs args){
        MaintWindowUtil.check(maintenanceProps)
        super.queueJob(args)
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
        Map data = [
            tempFileName: sourcePath.fileName.toString(),
            name: name
        ]
        Attachment attachment = attachmentRepo.create(data)
        return attachment.id
    }

    // SyncJob saveSyncJob(SyncJobArgs syncJobDto){
    //     new SyncJob(
    //         id: syncJobDto.jobId,
    //         jobType: syncJobDto.jobType,
    //
    //     )
    // }

    @Override
    SyncJob createSyncJob(SyncJobArgs args){
        SyncJob syncJob = new SyncJob(
            id: args.jobId,
            jobType: args.jobType,
            sourceId: args.sourceId,
            source: args.source,
            state: SyncJobState.Queued,
            params: args.asMap(),
            dataFormat: args.dataFormat
            //dataLayout: args.dataLayout
        )
        //if payloadId, then probably attachmentId with csv for example. Just store it and dont do payload conversion
        if(args.payloadId) {
            syncJob.payloadId = args.payloadId
        }
        else if(args.payload){
            String res = JsonEngine.toJson(args.payload, false)
            syncJob.payloadBytes = res.bytes
        }
        syncJob.persist(flush: true)
        return syncJob
    }

}
