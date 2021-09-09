package restify

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.IgnoreRest
import spock.lang.Specification

@Integration
class JobRestApiSpec extends Specification implements OkHttpRestTrait {
    JdbcTemplate jdbcTemplate

    String path = "/api/rally/org/bulk?source=Oracle"

    void "testing post Org with Job"() {
        given:
        List<Map> jsonList = [
            [num: "foox1", name: "Foox1", type: "Customer"],
            [num: "foox2", name: "Foox2", type: "Customer"],
            [num: "foox3", name: "Foox3", type: "Customer"],
        ]
        when:
        Response resp = post(path, jsonList)

        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.ok == true
        body.source == "Oracle"
        body.results != null
        body.results.size() == 3
        body.results[0].id != null

        //verify the bulk includes from restapi-config.xml
        body.results[0].source.sourceId == "foox1"
        body.results[0].num == "foox1"
        body.results[0].name == "Foox1"

    }

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
        body.results[2].item.num == "fooy2" //the original map would be returned back with error
        body.results[2].item.name == "fooy2"
        body.results[2].item.type == "Customer"
        body.results[2].error != null


        cleanup:
        jdbcTemplate.execute("DROP index org_source_unique")
    }
}
