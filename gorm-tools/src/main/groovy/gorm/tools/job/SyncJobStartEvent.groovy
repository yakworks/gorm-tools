package gorm.tools.job

class SyncJobStartEvent {
    Long jobId
    SyncJobContext syncJobContext

    static SyncJobStartEvent of(SyncJobContext ctx){
        return new SyncJobStartEvent(syncJobContext: ctx, jobId: ctx.jobId)
    }
}
