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
class TokenRestApiSpec extends Specification implements OkHttpRestTrait {

    def setup(){
        login()
    }

    Response doTokenPost(Map params){
        // Initialize Builder (not RequestBody)
        FormBody.Builder builder = new FormBody.Builder()
        params.each {
            builder.add( it.key, it.value )
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder()
            .url(getUrl("/api/token"))
            .addHeader("Authorization", "Bearer ${OkAuth.TOKEN}")
            .post(formBody)
            .build();
        Response resp = getHttpClient().newCall(request).execute()
        return resp
    }

    void "testing token grant_type token_exchange"() {
        when:
        //urn:ietf:params:oauth:grant-type:token-exchange
        Response resp = doTokenPost([requested_subject: "developers@9ci.com", grant_type: "token-exchange"])

        Map body = bodyToMap(resp)

        then:
        body.access_token
        body.sub == "developers@9ci.com"
    }

    void "testing token grant_type urn:ietf:params:oauth:grant-type:token-exchange"() {
        when:
        //urn:ietf:params:oauth:grant-type:token-exchange
        Response resp = doTokenPost([
            requested_subject: "developers@9ci.com",
            grant_type: "urn:ietf:params:oauth:grant-type:token-exchange"
        ])

        Map body = bodyToMap(resp)

        then:
        body.access_token
        body.sub == "developers@9ci.com"
    }

}
