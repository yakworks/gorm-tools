package restify

import gorm.tools.rest.JsonParserTrait
import gorm.tools.rest.client.OkHttpRestTrait
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import org.springframework.http.HttpStatus
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.orgs.model.Org

@Rollback
@Integration
@Ignore //FIXME - Since the job.data and job.results fields renamed, Gorm throws strange errors - StaticAPI not found, Gorm not initialized
class BulkRestApiSpec extends Specification implements OkHttpRestTrait, JsonParserTrait {
    String path = "/api/rally/org/bulk?jobSource=Oracle"

    void "verify bulk create and sanity check response"() {
        given:
        List<Map> jsonList = [
            [num: "foox1", name: "Foox1", type: "Customer", location: [ "street1": "string",  "street2": "string", "city": "string"]],
            [num: "foox2", name: "Foox2", type: "Customer"],
            [num: "foox3", name: "Foox3", type: "Customer"],
        ]
        when:
        Response resp = post(path, jsonList)

        Map body = bodyToMap(resp)

        then: "sanity check json structure"
        body.id != null
        body.ok == true
        body.state == "Finished"
        body.source == "Oracle" //should have been picked from query string
        body.data != null
        body.data.size() == 3
        body.data[0].id != null
        resp.code() == HttpStatus.CREATED.value()

        and: "verify the 'bulk' includes from restapi-config.xml"
        body.data[0].data.source.sourceId == "foox1"
        body.data[0].data.num == "foox1"
        body.data[0].data.name == "Foox1"

        when: "Verify created org"
        Org org = Org.get( body.data[0].id as Long)

        then:
        org != null
        org.num == "foox1"
        org.location != null
        org.location.street1 == "string"

        delete("/api/rally/org", body.data[0].id)
        delete("/api/rally/org", body.data[1].id)
        delete("/api/rally/org", body.data[2].id)
    }
}
