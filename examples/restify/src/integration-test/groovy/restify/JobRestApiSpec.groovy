package restify

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.HttpUrl
import okhttp3.Response
import org.springframework.http.HttpStatus
import spock.lang.Specification
import yakworks.rally.job.Job
import yakworks.rally.orgs.model.Contact

@Integration
class JobRestApiSpec extends Specification implements OkHttpRestTrait {

    String path = "/api/rally/org?source=Oracle"
    Map postData = [num:'foo1', name: "foo", type: 'Customer']

    void "testing post Org with Job"() {
        when:
        Response resp = post(path, [num: "foobie123", name: "foobie", type: "Customer"])

        Map body = bodyToMap(resp)

        then:
        // create Job record
        // pass in param.source to Job.source
        //
        when:
        Map params = [source:"Oracle"]
        Job job = Job.repo.update(params)

        resp.code() == HttpStatus.CREATED.value()
        body.id
        body.name == 'foobie'
        delete(path, body.id)
    }
}
