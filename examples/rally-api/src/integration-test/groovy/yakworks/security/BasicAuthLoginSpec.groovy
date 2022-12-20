package yakworks.security

import groovy.transform.CompileDynamic

import grails.testing.mixin.integration.Integration
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import spock.lang.Specification
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait

/**
 * test the legacy login with post username and password to login endpoint.
 */
@Integration
class BasicAuthLoginSpec extends Specification implements OkHttpRestTrait {

    //String endpoint = "/api/token"

    /** uses the basic auth to login and parse the access_token from response. */
    Response basicLogin(String uname, String pwd) {
        //create the basic auth credentials
        String basicAuth = Credentials.basic(uname, pwd)
        String lpath = "http://localhost:${serverPort}/api/token"
        // String lpath = "http://${username}:${password}@localhost:${serverPort}/api/token"
        Request request = new Request.Builder()
            .url(lpath)
            .addHeader("Authorization", basicAuth)
            .addHeader("Content-Type", jsonHeader)
            .method("POST", RequestBody.create("", null))
            .build()

        Response resp = getHttpClient().newCall(request).execute()
        assert resp.successful
        return resp
    }

    void "test login with json body"() {
        when:
        def resp = basicLogin('admin', '123')
        Map body = bodyToMap(resp)

        then:
        body.access_token
        //body.access_token == "foo"
    }

}
