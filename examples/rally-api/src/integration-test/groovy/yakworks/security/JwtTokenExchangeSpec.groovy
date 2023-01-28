package yakworks.security

import org.springframework.http.HttpStatus

import grails.testing.mixin.integration.Integration
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import spock.lang.Specification
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait

/**
 * test the controller
 */
@Integration
class JwtTokenExchangeSpec extends Specification implements OkHttpRestTrait {

    String endpoint = "/api/token-exchange"

    def setup(){
        login()
    }

    void "testing token-exchange"() {
        when:
        RequestBody formBody = new FormBody.Builder()
            .add("requested_subject", "developers@9ci.com")
            .build()
        Request request = new Request.Builder()
            .url(getUrl(endpoint))
            .addHeader("Authorization", "Bearer ${OkAuth.TOKEN}")
            .post(formBody)
            .build();

        Response resp = getHttpClient().newCall(request).execute()

        Map body = bodyToMap(resp)

        then:
        body.access_token
        body.sub == "developers@9ci.com"
    }

}
