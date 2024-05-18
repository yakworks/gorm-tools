/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.model.SourceType
import spock.lang.Specification
import yakworks.json.groovy.JsonEngine
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.config.MaintenanceProps
import yakworks.testing.gorm.unit.DataRepoTest
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class SyncJobSpec extends Specification implements GormHibernateTest, SecurityTest {
    static entityClasses = [SyncJob, Attachment, AttachmentLink]
    static springBeans = [
        //attachmentSupport:AttachmentSupport,
        syncJobService:DefaultSyncJobService,
        maintenanceProps: MaintenanceProps
    ]

    @Autowired DefaultSyncJobService syncJobService

    SyncJob createJob(){
        return new SyncJob([sourceType: SourceType.ERP, sourceId: 'ar/org']).persist(flush:true)
    }

    void "sanity check validation with String as data"() {
        expect:
        SyncJob job = new SyncJob([sourceType: SourceType.ERP, sourceId: 'ar/org'])
        job.validate()
        job.persist()
    }

    void "smoke test jobProps"() {
        expect:
        syncJobService.maintenanceProps.crons.size() == 2
    }

    void "kick off simulation of Job"() {
        when:
        // calls with Map data, Map args = [:]
        def dataList = ["id":1,"inactive":false,"name":"name"]
        def sourceId = "api/ar/org"
        def source = "Oracle"
        def sourceType = SourceType.RestApi
        def job = SyncJob.repo.create(payloadBytes:dataList.toString().bytes, source:source, sourceType: sourceType, sourceId:sourceId)

        then:
        job
        job.payloadBytes.size() > 0
    }

    void "check that problems save properly"() {
        when:
        // def errorList = ["ok":false,"tile":"bad stuff here"]
        def job = new SyncJob(problems: [["ok":false,"title":"error"]]).persist()
        def jobId = job.id
        // job.problems = [["ok":false,"title":"error"]]
        // job.persist(flush:true)
        flushAndClear()

        def job1 = SyncJob.get(jobId)

        then:
        job1
        job1.problems.size() == 1
        job1.problems[0].ok == false
        job1.problems[0].title == "error"
    }

    void "problems update"() {
        when:
        def job = new SyncJob().persist()
        def jobId = job.id
        flushAndClear()
        SyncJob.repo.update([id: jobId, problems: [["ok":false,"title":"error"]]])
        flushAndClear()

        def job1 = SyncJob.get(jobId)

        then:
        job1
        job1.problems.size() == 1
        job1.problems[0].ok == false
        job1.problems[0].title == "error"
    }

    void "convert json to byte array"() {
        setup:
        def res = JsonEngine.toJson(["One", "Two", "Three"])

        when:
        SyncJob job = new SyncJob(sourceType: SourceType.ERP, sourceId: 'ar/org', payloadBytes: res.bytes)
        def jobId = job.persist().id

        then: "get jobId"
        jobId

        when: "query the db for job we can read the data"
        SyncJob j = SyncJob.get(jobId)

        then:
        j
        res.bytes == j.payloadBytes
        res == j.payloadToString()
    }
}
