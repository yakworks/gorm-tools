package testing

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.CompileStatic

import gorm.tools.job.SyncJobService
import gorm.tools.repository.GormRepo
import yakworks.commons.util.BuildSupport

@CompileStatic
class TestSyncJobService implements SyncJobService<TestSyncJob> {

    @Override
    GormRepo<TestSyncJob> getRepo(){
        return TestSyncJob.repo
    }

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

}
