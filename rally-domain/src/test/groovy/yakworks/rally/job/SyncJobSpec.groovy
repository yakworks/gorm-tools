/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import yakworks.commons.json.JsonEngine
import yakworks.gorm.testing.SecurityTest
import gorm.tools.model.SourceType
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Ignore
import spock.lang.Specification

class SyncJobSpec extends Specification  implements DomainRepoTest<SyncJob>, SecurityTest {

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
        def job = SyncJob.repo.create(dataPayload:dataList, source:source, sourceType: sourceType, sourceId:sourceId)

        then:
        job
        job.requestData.size()>0
    }


    void "convert json to byte array"() {
        setup:
        def res = JsonEngine.toJson(["One", "Two", "Three"])

        when:
        SyncJob job = new SyncJob(sourceType: SourceType.ERP, sourceId: 'ar/org', requestData: res.bytes)
        def jobId = job.persist().id

        then: "get jobId"
        jobId

        when: "query the db for job we can read the data"
        SyncJob j = SyncJob.get(jobId)

        then:
        j
        res.bytes == j.requestData
        res == new String(j.requestData, 'UTF-8')

    }





}
