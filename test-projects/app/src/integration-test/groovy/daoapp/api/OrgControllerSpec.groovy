package daoapp.api

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.test.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.json.JSONElement
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Ignore
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder

@Integration
class OrgControllerSpec extends Specification {

    @Shared
    RestBuilder rest = new RestBuilder(new org.springframework.web.client.RestTemplate())

    @Shared daoapp.api.OrgController controller = new daoapp.api.OrgController()

    @Autowired
    WebApplicationContext ctx

    @Ignore
    protected GrailsWebRequest getCurrentRequestAttributes() {
        return (GrailsWebRequest)RequestContextHolder.currentRequestAttributes()
    }

    @Ignore
    protected String getControllerName() { return null }

    void setup() {
        MockHttpServletRequest request = new   GrailsMockHttpServletRequest(ctx.servletContext)
        MockHttpServletResponse response = new GrailsMockHttpServletResponse()
        GrailsWebMockUtil.bindMockWebRequest(ctx, request, response)
        currentRequestAttributes.setControllerName(controllerName)

    }


    @Ignore
    def autowire(def controller) {
        ctx.autowireCapableBeanFactory.autowireBeanProperties(controller, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false)
        controller
    }

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

    void "check GET for not found"() {
        when:
        RestResponse response = rest.get("${baseUrl}/org/500")

        then:
        response.status == 404
        response.json != null
        JSONElement json = response.json
        json.error == "Org not found with id 500:\n"
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

    void "check POST without name"() {
        when:
        RestResponse response = rest.post("${baseUrl}/org"){
            json([
                    lastName: "Test contact"
            ])
        }

        then:
        response.status == 201
        response.json != null
        JSONElement json = response.json
        json.name == "default from Dao"
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

    void "check PUT for not exist"() {
        when:
        RestResponse response = rest.put("${baseUrl}/org/500"){
            json([
                    name: "new Test contact"
            ])
        }

        then:
        response.status == 404
        response.json != null
        JSONElement json = response.json
        json.error == "Org not found with id 500:\n"
    }

    void "check DELETE request"() {
        when:
        RestResponse response = rest.delete("${baseUrl}/org/1")

        then:
        response.status == 200
        JSONElement json = response.json
        json.id == 1
    }

    void "check DELETE for not exist"() {
        when:
        RestResponse response = rest.delete("${baseUrl}/org/500")

        then:
        response.status == 404
        response.json != null
        JSONElement json = response.json
        json.error == "Org not found with id 500:\n"
    }


    void "check list with params Filter by Name eq"() {
        when:
        controller.params.putAll([criteria:[name: "Org#23"],max: 150])
        List list = controller.listAllResources()
        then:
        list.size() == 1
        list[0].name == "Org#23"
    }

    void "check list with params Filter by id eq"() {
        when:
        controller.params.putAll([criteria:[id: "24"], max: 150])
        List list = controller.listAllResources()
        then:
        list.size() == 1
        list[0].name == "Org#23"
    }

    void "check list with params Filter by id inList"() {
        when:
        controller.params.putAll([criteria:[id: ["24", "25"]], max: 150])
        List list = controller.listAllResources()
        then:
        list.size() == 2
        list[0].name == "Org#23"
    }

    void "check list with params Filter by Name ilike"() {
        when:
        controller.params.putAll([criteria:[name: "Org#2%"], max: 150])
        List list = controller.listAllResources()
        then:
        list.size() == 11
        list[0].name == "Org#2"
        list[1].name == "Org#20"
        list[10].name == "Org#29"
    }

    void "check list with params Filter by  nested id"() {
        when:
        controller.params.putAll([criteria:[address: [id: 2]], max: 150])
        List list = controller.listAllResources()
        then:
        list.size() == 1
        list[0].name == "Org#1"
        list[0].address.id == 2
    }

    void "check list with params Filter by nestedId"() {
        when:
        controller.params.putAll([criteria:[addressId: 2], max: 150])
        List list = controller.listAllResources()
        then:
        list.size() == 1
        list[0].name == "Org#1"
        list[0].address.id == 2
    }

    void "check list with params Filter by nested  id inList"() {
        when:
        controller.params.putAll([criteria:[address:[id: ["24", "25", "26"]]], max: 150])
        List list = controller.listAllResources()
        then:
        list.size() == 3
        list[0].name == "Org#23"
    }

    void "check list with params Filter by nested string"() {
        when:
        controller.params.putAll([criteria:[address: [city: "City#2"]], max: 150])
        List list = controller.listAllResources()
        then:
        list.size() == 1
        list[0].name == "Org#2"
        list[0].address.id == 3
    }

   /* void "check list with params Filter by by boolean"() {
        when:
        controller.params.putAll([criteria:[isActive: "true"], max: 150])
        List list = controller.listAllResources()
        then:
        list.size() ==  Org.createCriteria().list(){eq "isActive", true}.size()

    }*/

    void "check list with params Filter with `or`"() {
        when:
        controller.params.putAll([criteria:["\$or": ["name": "Org#1", "address.id": "4" ]], max: 150])
        List list = controller.listAllResources()
        then:
        list.size() == 2
        list[0].name == "Org#1"
        list[1].name == "Org#3"

    }









}
