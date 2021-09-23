/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import gorm.tools.json.Jsonify
import gorm.tools.security.testing.SecurityTest
import gorm.tools.source.SourceType
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Ignore
import spock.lang.Specification

class JobSpec extends Specification  implements DomainRepoTest<Job>, SecurityTest {

    void "sanity check validation with String as data"() {
        expect:
        Job job = new Job([sourceType: SourceType.ERP, sourceId: 'ar/org'])
        job.validate()
        job.persist()
    }

    @Ignore //XXX put ot back in when we add sourceId to be required in SourceTrait. Too many tests were failing
    void "sanity check validation no sourceId"() {
        when:
        Job job = new Job()
        // job.persist()
        then:
        job
        !job.validate()


    }

    void "kick off simulation of Job"() {
        when:
        // calls with Map data, Map args = [:]
        def dataList = ["id":1,"inactive":false,"name":"name"]
        def sourceId = "api/ar/org"
        def source = "Oracle"
        def sourceType = SourceType.RestApi
        def job = Job.repo.create(dataPayload:dataList, source:source, sourceType: sourceType, sourceId:sourceId)

        then:
        job
        job.requestData.size()>0
    }


    void "convert json to byte array"() {
        setup:
        def res = Jsonify.render(["One", "Two", "Three"])

        when:
        Job job = new Job(sourceType: SourceType.ERP, sourceId: 'ar/org', requestData: res.jsonText.bytes)
        def jobId = job.persist().id

        then: "get jobId"
        jobId

        when: "query the db for job we can read the data"
        Job j = Job.get(jobId)

        then:
        j
        res.jsonText.bytes == j.requestData
        res.jsonText == new String(j.requestData, 'UTF-8')

    }





}
