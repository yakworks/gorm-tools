package restify

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.HttpUrl
import okhttp3.Response
import org.bouncycastle.crypto.engines.EthereumIESEngine
import org.springframework.http.HttpStatus
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.job.Job
import yakworks.rally.orgs.model.Contact

@Integration
class JobRestApiSpec extends Specification implements OkHttpRestTrait {

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
        body.results != null
        body.results.size() == 3
    }
}
