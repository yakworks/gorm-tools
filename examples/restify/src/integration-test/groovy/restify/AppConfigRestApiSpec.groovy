package restify

import gorm.tools.rest.client.OkHttpRestTrait
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Specification

// import grails.gorm.transactions.Rollback

@Integration
class AppConfigRestApiSpec extends Specification implements OkHttpRestTrait {

    void "test config values"() {
        when:
        Response resp = get('/api/appConfig/org')
        Map body = bodyToMap(resp)

        then: "should have exluded the flattened spring array keys"
        body.includes.get == ['*', 'type.*', 'status.*']
        body.includes.picklist == ['id', 'name']
        !body.includes.containsKey("get[0]")
        body.form[0].selectOptions.dataApiParams.or != null
        body.form[0].selectOptions.dataApiParams."or[0]" == null
    }



}
