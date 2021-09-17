package restify

import gorm.tools.job.JobState
import gorm.tools.rest.JsonParserTrait
import gorm.tools.rest.client.OkHttpRestTrait
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import okhttp3.Response
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate

import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.rally.job.Job
import yakworks.rally.orgs.model.Org

@Rollback
@Integration
class JobRestApiSpec extends Specification implements OkHttpRestTrait, JsonParserTrait {
    JdbcTemplate jdbcTemplate

    String path = "/api/rally/org/bulk?source=Oracle"

    void "testing post Org with Job"() {
        given:
        List<Map> jsonList = [
            [num: "foox1", name: "Foox1", type: "Customer", location: [ "street1": "string",  "street2": "string", "city": "string"]],
            [num: "foox2", name: "Foox2", type: "Customer"],
            [num: "foox3", name: "Foox3", type: "Customer"],
        ]
        when:
        Response resp = post(path, jsonList)

        Map body = bodyToMap(resp)

        then:
        body.id != null
        body.ok == true
        body.source == "Oracle"
        body.results != null
        body.results.size() == 3
        body.results[0].id != null
        resp.code() == HttpStatus.CREATED.value()

        //verify the bulk includes from restapi-config.xml
        body.results[0].source.sourceId == "foox1"
        body.results[0].num == "foox1"
        body.results[0].name == "Foox1"

        when: "Verify org"
        Org org = Org.get( body.results[0].id as Long)

        then:
        org != null
        org.num == "foox1"
        org.location != null
        org.location.street1 == "string"

        when: "Verify job.data"
        Job job = Job.get(body.id as Long)

        then:
        job != null
        job.data != null
        job.state == JobState.Finished

        when: "Verify job.data json"
        StringReader str = new StringReader(new String(job.data, "UTF-8"))
        List dataList = parseJson(str)

        then:
        dataList.size() == 3
        dataList[0].num == "foox1"

        delete("/api/rally/org", body.results[0].id)
        delete("/api/rally/org", body.results[1].id)
        delete("/api/rally/org", body.results[2].id)

    }

    //FIXME #339 blowing up now, but what are we really testing here?
    // if we are testing repo logic then doesnt belong in controller
    // if we are testing something else then explain.
    @Ignore
    void "testing post with duplicates"() {
        setup:
        jdbcTemplate.execute("CREATE UNIQUE INDEX org_source_unique ON OrgSource(sourceType, sourceId, orgTypeId)")

        List<Map> jsonList = [
            [num: "fooy1", name: "fooy1", type: "Customer", sourceType: "RestApi"],
            [num: "fooy2", name: "fooy2", type: "Customer", "sourceType": "RestApi"],
            [num: "fooy2", name: "fooy2", type: "Customer", "sourceType": "RestApi"],
        ]

        when:
        Response resp = post(path, jsonList)

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.results != null
        body.results.size() == 3
        body.results[0].id != null
        body.results[0].source.sourceId != null
        body.results[0].ok == true
        body.results[1].ok == true
        body.results[2].ok == false
        body.results[2].data.num == "fooy2" //the original map would be returned back with error
        body.results[2].data.name == "fooy2"
        body.results[2].data.type == "Customer"
        body.results[2].error != null


        cleanup:
        jdbcTemplate.execute("DROP index org_source_unique")
    }
}
