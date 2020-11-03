package restify

import geb.spock.GebSpec
import gorm.tools.rest.client.RestApiTestTrait
import grails.testing.mixin.integration.Integration

import static org.springframework.http.HttpStatus.CREATED

// @Integration(applicationClass = Application)
@Integration
class TaskRestApiSpec extends GebSpec implements RestApiTestTrait {
    String path = "api/task"

    //@Transactional
    //Map getPostData() { return TestDataJson.buildMap(Task, project: Project.get(1)) }
    Map postData = [name: "task", project: [id: 1]]

    Map putData = [name: "Task Update"]

    Map invalidData = ["name": null]

    List<String> getExcludes() {
        ['lastUpdated', 'dateCreated']
    }

    void "exercise api"() {
        expect:
        testGet()
        testPost()
        testPut()
        testDelete()
    }

    @Override
    def testPost() {
        // "The save action is executed with valid data"
        def response = restBuilder.post(resourcePath) {
            json postData
        }

        assert response.status == CREATED.value()
        //response.json.id
        assert response.json.name == 'task'
        assert response.json.project.id == 1

        def rget = restBuilder.get("$resourcePath/${response.json.id}")
        assert rget.json.name == 'task'
        assert rget.json.project.id == 1
        return rget
    }
}
