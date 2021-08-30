/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import gorm.tools.json.Jsonify
import gorm.tools.source.SourceType
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification

class JobSpec extends Specification  implements DomainRepoTest<Job> {

    void "sanity check validation with String as data"() {
        expect:
        Job job = new Job([sourceType: SourceType.ERP, sourceId: 'ar/org'])
        job.validate()
        job.persist()
        job.source == "foo"
    }

    void "sanity check validation no sourceId"() {
        when:
        Job job = new Job()
        // job.persist()
        then:
        job
        !job.validate()


    }

    // void "kick off simulation of Job"() {
    //     when:
    //     //List<Map> dataList, Map args = [:]
    //     def dataList = ["One", "Two", "Three"]
    //     def endPoint = "api/ar/org"
    //     def sourceName = "Oracle"
    //     def sourceType = SourceType.RestApi
    //     Job job = JobRepo.create(dataPayload:dataList, source:sourceName, sourceId:endPoint, sourceType: sourceType )
    //
    //     // in before create method:
    //     // check if dataPayload
    //     def res = Jsonify.render(dataPayload)
    //     job.data = res.jsonText.bytes
    // }


    void "convert json to byte array"() {
        setup:
        def res = Jsonify.render(["One", "Two", "Three"])

        when:
        Job job = new Job(sourceType: SourceType.ERP, sourceId: 'ar/org', data:res.jsonText.bytes)
        def jobId = job.persist().id

        then: "get jobId"
        jobId

        when: "query the db for job we can read the data"
        Job j = Job.get(jobId)

        then:
        j
        res.jsonText.bytes == j.data
        res.jsonText == new String(j.data, 'UTF-8')

    }





}
