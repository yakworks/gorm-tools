/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.model.DataOp
import spock.lang.Specification
import yakworks.api.problem.ThrowableProblem
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.config.JobProps
import yakworks.rally.config.MaintenanceProps
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.TagLink
import yakworks.spring.AppResourceLoader
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class DefaultSyncJobServiceSpec extends Specification implements GormHibernateTest, SecurityTest {
    static entityClasses = [SyncJob, Attachment, AttachmentLink, TagLink]
    static springBeans = [
        AttachmentSupport,
        AppResourceLoader,
        MaintenanceProps,
        JobProps,
        DefaultSyncJobService
    ]

    @Autowired DefaultSyncJobService syncJobService
    // @Autowired JobProps jobProps
    @Autowired MaintenanceProps maintenanceProps

    void "smoke test jobProps"() {
        expect:
        syncJobService.maintenanceProps.crons.size() == 2
    }

    void "test queueJob"() {
        when:
        List payload = []
        (1..1001).each {
            payload << it
        }

        Map params = [
            foo: 'bar', dataOp: 'add'
        ]
        SyncJobArgs syncJobArgs = SyncJobArgs.of(DataOp.add)
            .source("Some_Source")
            .sourceId('123')
            .payload(payload)
            .entityClass(Org)
            .jobType('foo')
            .params(params)

        SyncJobEntity job = syncJobService.queueJob(syncJobArgs)
        flushAndClear()

        SyncJob syncJob = SyncJob.get(job.id)

        then:
        noExceptionThrown()
        job.id
        syncJob.state.name() == "Queued"
        syncJob.jobType == 'foo'
        syncJob.source == syncJobArgs.source
        syncJob.sourceId == syncJobArgs.sourceId
        syncJob.payloadId
        Attachment.get(syncJob.payloadId).name.startsWith("Sync")
        //check params
        syncJob.params.keySet().size() == 2
        syncJob.params['foo']
        syncJob.params['dataOp'] == 'add'

        cleanup:
        Attachment.repo.removeById(syncJob.payloadId)
    }

    void "test createJob"() {
        when:
        maintenanceProps.crons = []
        SyncJobArgs syncJobArgs = new SyncJobArgs(
            sourceId: '123', source: 'some source', jobType: 'foo'
        )
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
