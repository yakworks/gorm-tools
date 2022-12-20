package yakworks.rest

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import yakworks.rest.gorm.controller.RestRepoApiController
import yakworks.rally.orgs.model.Org
import yakworks.testing.rest.RestIntTest

@Rollback
@Integration
class OrgMangoControllerTests extends RestIntTest {

    RestRepoApiController<Org> controller

    void setup() {
        controllerName = 'OrgController'
    }

    void "list mango sum groupby"() {
        when:
        controller.params << [
            projections:'calc.totalDue:"sum",type:"group"',
            sort:'calc_totalDue:asc'
        ]
        controller.list()
        Map body = response.bodyToMap()
        List data = body.data

        then:
        response.status == 200
        data.size() == 5
        data[0].type.name == 'Client'
        data[0]['calc_totalDue'] < data[1]['calc_totalDue']
        data[1]['calc_totalDue'] < data[2]['calc_totalDue']
    }

    void "paging in projections "() {
        when:
        controller.params << [
            projections: 'calc.totalDue:"sum",num:"group"',
            max        : '5'
        ]
        controller.list()
        Map body = response.bodyToMap()
        List data = body.data

        then:
        body.page == 1
        body.data.size() == 5
        body.total == 20
        body.records == 100
    }

    void "list CSV"() {
        // ?max=20&page=1&q=%7B%7D&sort=org.calc.totalDue
        when:
        controller.params << [
            projections:'calc.totalDue:"sum",type:"group"',
            sort:'calc_totalDue:asc',
            format:'csv'
        ]
        controller.list()
        // Map body = response.bodyToMap()
        // List data = body.data

        then:
        response.contentAsString.startsWith('"type.id","type.name","calc_totalDue"')
        response.status == 200
        response.header("Content-Type").contains("text/csv")


    }

    void "list xlsx"() {
        // ?max=20&page=1&q=%7B%7D&sort=org.calc.totalDue
        when:
        controller.params << [
            projections:'calc.totalDue:"sum",type:"group"',
            sort:'calc_totalDue:asc',
            format:'xlsx'
        ]
        controller.list()

        then:
        response.status == 200
        response.header("Content-Type").contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

}
