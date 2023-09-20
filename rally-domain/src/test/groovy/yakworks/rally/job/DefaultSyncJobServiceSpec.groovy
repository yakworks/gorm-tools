/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.model.SourceType
import spock.lang.Specification
import yakworks.api.problem.ThrowableProblem
import yakworks.json.groovy.JsonEngine
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.testing.gorm.unit.DataRepoTest
import yakworks.testing.gorm.unit.SecurityTest

class DefaultSyncJobServiceSpec extends Specification implements DataRepoTest, SecurityTest {
    static entityClasses = [SyncJob, Attachment]
    static springBeans = [
        attachmentSupport: AttachmentSupport,
        syncJobService   : DefaultSyncJobService,
        jobProps         : JobProps
    ]

    @Autowired DefaultSyncJobService syncJobService
    @Autowired JobProps jobProps

    void "smoke test jobProps"() {
        expect:
        syncJobService.jobProps.maintenanceWindow.size() == 2
    }

    void "test createJob"() {
        when:
        jobProps.maintenanceWindow = []
        SyncJobArgs syncJobArgs = new SyncJobArgs(sourceId: '123', source: 'some source')
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, [])
        then:
        noExceptionThrown()

        when:
        //force error with catch all cron expresions
        jobProps.maintenanceWindow = ['* * 0-23 * * MON-SUN']
        jobContext = syncJobService.createJob(syncJobArgs, [])
        then:
        var e = thrown(ThrowableProblem)
        e.status.code == 503
    }

}
