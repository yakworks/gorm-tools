/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import spock.lang.Specification
import yakworks.api.problem.ThrowableProblem
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.config.JobProps
import yakworks.rally.config.MaintenanceProps
import yakworks.testing.gorm.unit.DataRepoTest
import yakworks.testing.gorm.unit.SecurityTest

class DefaultSyncJobServiceSpec extends Specification implements DataRepoTest, SecurityTest {
    static entityClasses = [SyncJob, Attachment]
    static springBeans = [
        attachmentSupport: AttachmentSupport,
        syncJobService   : DefaultSyncJobService,
        maintenanceProps : MaintenanceProps,
        jobProps : JobProps
    ]

    @Autowired DefaultSyncJobService syncJobService
    // @Autowired JobProps jobProps
    @Autowired MaintenanceProps maintenanceProps

    void "smoke test jobProps"() {
        expect:
        syncJobService.maintenanceProps.crons.size() == 2
    }

    void "test createJob"() {
        when:
        maintenanceProps.crons = []
        SyncJobArgs syncJobArgs = new SyncJobArgs(sourceId: '123', source: 'some source')
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, [])
        then:
        noExceptionThrown()

        when:
        //force error with catch all cron expresions that makes ever hour of every day the window
        maintenanceProps.crons = ['* * 0-23 * * MON-SUN']
        jobContext = syncJobService.createJob(syncJobArgs, [])
        then:
        var e = thrown(ThrowableProblem)
        e.status.code == 503
    }

}
