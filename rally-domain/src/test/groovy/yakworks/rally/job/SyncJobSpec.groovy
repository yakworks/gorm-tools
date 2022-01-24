/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import gorm.tools.model.SourceType
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Specification
import yakworks.commons.json.JsonEngine
import yakworks.gorm.testing.SecurityTest
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment

class SyncJobSpec extends Specification implements DataRepoTest, SecurityTest {

    Closure doWithDomains() { { ->
        attachmentSupport(AttachmentSupport)
        syncJobService(DefaultSyncJobService)
    }}

    void setupSpec() {
        mockDomains(SyncJob, Attachment)
    }

    void "sanity check validation with String as data"() {
        expect:
        SyncJob job = new SyncJob([sourceType: SourceType.ERP, sourceId: 'ar/org'])
        job.validate()
        job.persist()
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
