package restify

import geb.spock.GebSpec
import gorm.tools.rest.testing.RestApiFuncSpec
import gorm.tools.rest.testing.RestApiTestTrait
import gorm.tools.testing.TestDataJson
import gorm.tools.testing.TestTools
import grails.gorm.transactions.Rollback
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import yakworks.taskify.domain.Org
import yakworks.taskify.domain.OrgType

import static org.springframework.http.HttpStatus.CREATED

@Integration
@Rollback
class OrgRestSpec extends RestApiFuncSpec {

    String path = "api/org"

    //@Transactional
    //Map getPostData() { return TestDataJson.buildMap(Org, type: OrgType.load(1)) }
    Map postData = [num:'foo1', name: "foo", type: [id: 1]]

    Map putData = [name: "Name Update"]

    Map invalidData = ["name": null]

    @Override
    def testPost() {
        // "The save action is executed with valid data"
        def response = restBuilder.post(resourcePath) {
            json postData
        }

        assert response.status == CREATED.value()
        //response.json.id
        assert response.json.name == 'foo'
        assert response.json.type.id == 1
        def rget = restBuilder.get("$resourcePath/${response.json.id}")
        assert rget.json.name == 'foo'
        assert rget.json.type.id == 1
        return rget
    }

}
