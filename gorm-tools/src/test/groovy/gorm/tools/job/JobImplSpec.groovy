/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import gorm.tools.json.Jsonify
import gorm.tools.source.SourceType
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification
import testing.*

class JobImplSpec extends Specification  implements DomainRepoTest<JobImpl> {

    void "sanity check validation with String as data"() {
        expect:
        JobImpl job = new JobImpl([sourceType: SourceType.ERP, sourceId: 'ar/org'])
        job.validate()
        job.persist()
        //job.source == "foo"  //XXX It should pick up JobImplRepo
    }

    void "sanity check validation no sourceId"() {
        when:
        JobImpl job = new JobImpl()
        // job.persist()
        then:
        job
        !job.validate()


    }


    void "convert json to byte array"() {
        setup:
        def res = Jsonify.render(["One", "Two", "Three"])

        when:
        JobImpl job = new JobImpl(sourceType: SourceType.ERP, sourceId: 'ar/org', data:res.jsonText.bytes)
        def jobId = job.persist().id

        then: "get jobId"
        jobId

        when: "query the db for job we can read the data"
        JobImpl j = JobImpl.get(jobId)

        then:
        j
        res.jsonText.bytes == j.data
        res.jsonText == new String(j.data, 'UTF-8')

    }





}