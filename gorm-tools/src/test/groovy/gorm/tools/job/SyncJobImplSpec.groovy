/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import org.springframework.core.task.SimpleAsyncTaskExecutor

import gorm.tools.model.SourceType
import spock.lang.Specification
import testing.TestSyncJob
import yakworks.json.groovy.JsonEngine
import yakworks.testing.gorm.unit.DataRepoTest
import yakworks.testing.gorm.unit.GormHibernateTest

class SyncJobImplSpec extends Specification  implements GormHibernateTest{
    static List entityClasses = [TestSyncJob]

    void "sanity check validation with String as data"() {
        expect:
        TestSyncJob job = new TestSyncJob(
            state: SyncJobState.Running,
            sourceType: SourceType.ERP, sourceId: 'ar/org'
        )
        job.validate()
        job.persist()
    }

    void "convert json to byte array"() {
        setup:
        String res = JsonEngine.toJson(["One", "Two", "Three"])

        when:
        TestSyncJob job = new TestSyncJob(sourceType: SourceType.ERP, sourceId: 'ar/org', payloadBytes: res.bytes)
        def jobId = job.persist().id

        then: "get jobId"
        jobId

        when: "query the db for job we can read the data"
        TestSyncJob j = TestSyncJob.get(jobId)

        then:
        j
        res == j.payloadToString()

    }

}
