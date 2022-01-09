package testing

import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.job.SyncJobState
import gorm.tools.repository.GormRepo
import groovy.transform.CompileStatic
import yakworks.api.ApiResults
import yakworks.commons.util.BuildSupport

import java.nio.file.Path
import java.nio.file.Paths

@CompileStatic
class TestSyncJobService implements SyncJobService<TestSyncJob> {

    @Override
    GormRepo<TestSyncJob> getRepo(){
        return TestSyncJob.repo
    }

    @Override
    Path createTempFile(String filename){
        return Paths.get(BuildSupport.gradleProjectDir, "build/bulk/${filename}")
    }

    @Override
    Long createAttachment(Map params) {
        //stub it out for testing, these dont support attachments, use integration and concrete
        // implementation to test attachments
        return 1
    }

}
