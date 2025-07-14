package gorm.tools.job

import groovy.json.JsonOutput

import com.fasterxml.jackson.databind.util.RawValue
import gorm.tools.model.SourceType
import spock.lang.Specification
import testing.TestSyncJob
import yakworks.commons.map.Maps
import yakworks.etl.DataMimeTypes
import yakworks.gorm.api.bulk.BulkImportJobArgs
import yakworks.json.groovy.JsonEngine
import yakworks.testing.rest.MockRestRequest

class JobUtilsSpec extends Specification  {

    TestSyncJob mockTestJob(){
        def job = new TestSyncJob(
            id: 1,
            ok: true,
            message: 'foo',
            state: SyncJobState.Finished,
            jobType: BulkImportJobArgs.JOB_TYPE,
            sourceType: SourceType.ERP,
            sourceId: 'ar/org',
            source: 'ERP',
            dataFormat: DataMimeTypes.json
        )
        job.dataBytes = JsonEngine.toJson(["One", "Two", "Three"]).bytes
        return job
    }

    void "test requestToSourceId"() {
        when:
        def req = new MockRestRequest()
        req.requestURI = "foo/bar/baz"
        req.queryString = "a=b&b=c"
        def sourceId = JobUtils.requestToSourceId(req)
        then:
        sourceId == "GET foo/bar/baz?a=b&b=c"

    }

    void "test jobToMapGroovy"() {
        when:
        def job = mockTestJob()
        def map = JobUtils.jobToMapGroovy(job)

        then:
        map.data.toString() == JsonOutput.unescaped(job.dataToString()).toString()
        Maps.omit(map, ['data']) == [
            id: 1,
            ok: true,
            state: 'Finished',
            jobType: 'bulk.import',
            message: 'foo',
            source: 'ERP',
            sourceId: 'ar/org',
            dataFormat: 'json',
            dataId: null,
            payloadId: null
        ]

    }

    void "test jobToMapJackson"() {
        when:
        def job = mockTestJob()
        def map = JobUtils.jobToMapJackson(job)

        then:
        map == [
            id: 1,
            ok: true,
            state: 'Finished',
            jobType: 'bulk.import',
            message: 'foo',
            source: 'ERP',
            sourceId: 'ar/org',
            dataFormat: 'json',
            dataId: null,
            payloadId: null,
            data: new RawValue(job.dataToString())
        ]

    }

    void "test commonJobToMap"() {
        when:
        def map = JobUtils.commonJobToMap(mockTestJob())

        then:
        map == [
            id: 1,
            ok: true,
            state: 'Finished',
            jobType: 'bulk.import',
            message: 'foo',
            source: 'ERP',
            sourceId: 'ar/org',
            dataFormat: 'json',
            dataId: null,
            payloadId: null
        ]


    }

}
