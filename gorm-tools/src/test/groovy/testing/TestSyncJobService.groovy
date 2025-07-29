package testing

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.CompileStatic

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.job.SyncJobState
import gorm.tools.repository.GormRepo
import yakworks.commons.util.BuildSupport
import yakworks.json.groovy.JsonEngine

/**
 * NOTE: this is here just to get test passing.
 * The main logic is in the DefaultSyncService in rally
 */
@CompileStatic
class TestSyncJobService extends SyncJobService<TestSyncJob> {

    @Override
    GormRepo<TestSyncJob> getRepo(){
        return TestSyncJob.repo
    }

    /**
     * In test we queue it and run it at same time
     */
    // @Override
    // SyncJobEntity queueJob(SyncJobArgs args, SyncJobState state = SyncJobState.Queued) {
    //     SyncJobEntity sje = super.queueJob(args, state)
    // }

    @Override
    Path createTempFile(String filename){
        def path = Paths.get(BuildSupport.projectDir, "build/bulk")
        Files.createDirectories(path)
        return path.resolve(filename)
    }

    @Override
    Long createAttachment(Path path, String name) {
        //stub it out for testing, these dont support attachments, use integration and concrete
        // implementation to test attachments
        return 1
    }

    @Override
    TestSyncJob createSyncJob(SyncJobArgs args){
        TestSyncJob syncJob = new TestSyncJob(
            id: args.jobId,
            jobType: args.jobType,
            sourceId: args.sourceId,
            source: args.source,
            state: SyncJobState.Queued,
            params: args.asMap(),
            dataFormat: args.dataFormat,
            //dataLayout: args.dataLayout
        )
        //if payloadId, then probably attachmentId with csv for example. Just store it and dont do payload conversion
        if(args.payloadId) {
            syncJob.payloadId = args.payloadId
        }
        else if(args.payload){
            String res = JsonEngine.toJson(args.payload)
            syncJob.payloadBytes = res.bytes
        }
        syncJob.persist(flush: true)
        return syncJob
    }
}
