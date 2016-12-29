package daoapp.api

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.test.mixin.integration.Integration
import org.grails.web.json.JSONElement
import spock.lang.Shared
import spock.lang.Specification

@Integration
class OrgControllerSpec extends Specification {

    @Shared
    RestBuilder rest = new RestBuilder()

    def getBaseUrl(){"http://localhost:${serverPort}/api"}

    void "check GET list request without params "() {
        when:
        RestResponse response = rest.get("${baseUrl}/org")

        then:
        response.status == 200
        response.json != null
        JSONElement json = response.json
        //by default max value is 10 rows
        json.total == 10
        json.page == 1
        json.records == 100
        def rows = json.rows
        rows.size() == 10
    }

    void "check GET list request with max parameter"() {
        when: "list endpoint with max param"
        RestResponse response = rest.get("${baseUrl}/org?max=20")

        then:
        response.status == 200
        response.json != null
        JSONElement json = response.json
        json.total == 5
        json.page == 1
        json.records == 100
        def rows = json.rows
        rows.size() == 20
    }

    void "check GET list request with page"() {
        when: "list endpoint with max param"
        RestResponse response = rest.get("${baseUrl}/org?page=2")

        then:
        response.status == 200
        response.json != null
        JSONElement json = response.json
        json.total == 10
        json.page == 2
        json.records == 100
        def rows = json.rows
        rows.size() == 10
    }

    void "check GET by id"() {
        when:
        RestResponse response = rest.get("${baseUrl}/org/1")

        then:
        response.status == 200
        response.json != null
        JSONElement json = response.json
        json.name == "Org#0"
    }

    void "check POST request"() {
        when:
        RestResponse response = rest.post("${baseUrl}/org"){
            json([
                    name: "Test contact"
            ])
        }

        then:
        response.status == 201
        response.json != null
        JSONElement json = response.json
        json.name == "Test contact from Dao"
    }



    void "check PUT request"() {
        when:
        RestResponse response = rest.put("${baseUrl}/org/101"){
            json([
                    name: "new Test contact"
            ])
        }

        then:
        response.status == 200
        response.json != null
        JSONElement json = response.json
        json.id == 101
        json.name == "new Test contact"
    }

    void "check DELETE request"() {
        when:
        RestResponse response = rest.delete("${baseUrl}/org/1")

        then:
        response.status == 200
        JSONElement json = response.json
        json.id == 1
    }

}
