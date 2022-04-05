package gorm.tools.job

class SyncJobFinishedEvent {
    Long jobId
    Boolean ok
    SyncJobContext syncJobContext

    static SyncJobFinishedEvent of(SyncJobContext ctx){
        return new SyncJobFinishedEvent(syncJobContext: ctx, jobId: ctx.jobId, ok: ctx.ok.get())
    }
}
