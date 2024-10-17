package yakworks.rest

import org.springframework.http.HttpStatus

import gorm.tools.transaction.WithTrx
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag
import yakworks.rest.client.OkHttpRestTrait

@Integration
class CacheListApiSpec extends Specification implements OkHttpRestTrait, WithTrx {

    String path = "/api/rally/org"

    def setup(){
        login()
    }

    void "test list timeout"() {
        when:
        String listPath = "$path?qSearch=org&sleep=20"
        //run first one
        enqueue("GET", listPath)
        sleep(100)
        //this one should timeout
        Response resp = get(listPath)
        Map body = bodyToMap(resp)

        then:
        body.status == 429
        resp.code() == HttpStatus.TOO_MANY_REQUESTS.value()

    }

}
