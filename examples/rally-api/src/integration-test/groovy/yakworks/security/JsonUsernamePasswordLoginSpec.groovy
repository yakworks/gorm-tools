package yakworks.security

import grails.testing.mixin.integration.Integration
import okhttp3.Request
import okhttp3.Response
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rest.client.OkAuth
import yakworks.rest.client.OkHttpRestTrait

/**
 * test the legacy login with post username and password to login endpoint.
 */
@Ignore
@Integration
class JsonUsernamePasswordLoginSpec extends Specification implements OkHttpRestTrait {

    String endpoint = "/api/login"

    /** uses the basic auth to login and parse the access_token from response. */
    Map jsonLogin(String uname, String pwd) {
        //create the basic auth credentials
        // String basicAuth = Credentials.basic(uname, pwd)
        String lpath = "http://localhost:${serverPort}${endpoint}"
        // String lpath = "http://${username}:${password}@localhost:${serverPort}/api/token"
        Map postBody = [username: uname, password: pwd]

        Request request = new Request.Builder()
            .url(lpath)
            .addHeader("Content-Type", jsonHeader)
            .method("POST", getRequestBody("POST", postBody))
            .build()

        Response resp = getHttpClient().newCall(request).execute()
        assert resp.successful
        Map body = bodyToMap(resp)
        // OkAuth.TOKEN = body.access_token
        // OkAuth.BEARER_TOKEN = "Bearer ${body.access_token}"
        return body
    }

    void "test login with json body"() {
        when:
        Map body = jsonLogin('admin', '123')

        then:
        body.access_token
        //body.access_token == "foo"

        cleanup:
        OkAuth.TOKEN = null
    }

}
