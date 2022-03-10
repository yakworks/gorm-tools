package restify

import gorm.tools.rest.controller.RestRepoApiController
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.commons.map.Maps
import yakworks.gorm.testing.http.RestIntegrationTest
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.tag.model.Tag

@Rollback
@Integration
class OrgMangoControllerTests extends Specification implements RestIntegrationTest {

    RestRepoApiController<Org> controller

    void setup() {
        controllerName = 'OrgController'
    }

    void "list mango sum groupby"() {
        when:
        controller.params << [
            projections:'calc.totalDue:"sum",`type:"group"`',
            sort:'calc_totalDue_sum:asc'
        ]
        controller.list()
        Map body = response.bodyToMap()
        List data = body.data

        then:
        response.status == 200
        data.size() == 5
        data[0].type.name == 'Client'
        data[0]['calc_totalDue_sum'] < data[1]['calc_totalDue_sum']
        data[1]['calc_totalDue_sum'] < data[2]['calc_totalDue_sum']
    }

    @Ignore //https://github.com/yakworks/gorm-tools/issues/482
    void "paging in projections "() {
        when:
        controller.params << [
            projections:'calc.totalDue:"sum",num:"group"',
            max:'2'
        ]
        controller.list()
         Map body = response.bodyToMap()
         List data = body.data

        then:
        body.page == 1
        body.total == 50  // right now 1
        body.records == 100  //right now 2
        body.data
        data.size() == 2
    }

    void "list CSV"() {
        // ?max=20&page=1&q=%7B%7D&sort=org.calc.totalDue
        when:
        controller.params << [
            projections:'calc.totalDue:"sum",type:"group"',
            sort:'calc_totalDue_sum:asc',
            format:'csv'
        ]
        controller.list()
        // Map body = response.bodyToMap()
        // List data = body.data

        then:
        response.contentAsString.startsWith('"type.id","type.name","calc_totalDue_sum"')
        response.status == 200
        response.header("Content-Type").contains("text/csv")


    }

}
