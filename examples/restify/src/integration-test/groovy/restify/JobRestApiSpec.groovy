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

    String path = "/api/rally/org?source=Oracle"
    Map postData = [num:'foo1', name: "foo", type: 'Customer']

    @Ignore // XXX https://github.com/9ci/domain9/issues/331 Not sure if we need it and if it's in a good place
    void "testing post Org with Job"() {
        when:
        Response resp = post(path, [num: "foobie123", name: "foobie", type: "Customer"])

        Map body = bodyToMap(resp)

        Map params = [source:"Oracle"]
        Job job = Job.repo.update(params)

        resp.code() == HttpStatus.CREATED.value()
        body.id
        body.name == 'foobie'
        delete(path, body.id)

        then:
        job
    }
}
