package yakworks.rest

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

import yakworks.rest.gorm.controller.CrudApiController
import yakworks.rally.orgs.model.Org
import yakworks.testing.rest.RestIntTest

@Rollback
@Integration
class OrgMangoControllerTests extends RestIntTest {

    CrudApiController<Org> controller

    void setup() {
        controllerName = 'OrgController'
    }

    void "list mango sum groupby"() {
        when:
        request.addParameters(
            q:"*",
            projections:'calc.totalDue:"sum",type:"group"',
            sort:'calc_totalDue_sum:asc'
        )
        controller.list()
        Map body = response.bodyToMap()
        List data = body.data

        then:
        response.status == 200
        data.size() == 5
        data[0].type.name == 'Client'
        data[0]['calc']['totalDue'] < data[1]['calc']['totalDue']
        data[1]['calc']['totalDue'] < data[2]['calc']['totalDue']
    }

    void "list mango full monty"() {
        when:
        controller.params << [
            q:"*",
            projections:'"calc.totalDue as Balance":"sum","calc.totalDue as MaxDue":"max","type":"group"',
            sort:'Balance:asc'
        ]
        controller.list()
        Map body = response.bodyToMap()
        List data = body.data

        then:
        response.status == 200
        data.size() == 5
        data[0].type.name == 'Client'
        data[0]['Balance'] < data[1]['Balance']
        data[1]['Balance'] < data[2]['Balance']
    }

    void "paging in projections "() {
        when:
        request.addParameters(
            q:"*",
            projections: 'calc.totalDue:"sum",num:"group"',
            max        : '5'
        )
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
        request.addParameters(
            q:"*",
            projections:'calc.totalDue:"sum",type:"group"',
            sort:'calc_totalDue_sum:asc',
            format:'csv'
        )
        controller.list()
        // Map body = response.bodyToMap()
        // List data = body.data

        then:
        response.contentAsString.startsWith('"type.id","type.name","calc.totalDue')
        response.status == 200
        response.header("Content-Type").contains("text/csv")


    }

    void "list xlsx"() {
        // ?max=20&page=1&q=%7B%7D&sort=org.calc.totalDue
        when:
        controller.params << [
            q:"*",
            projections:'calc.totalDue:"sum",type:"group"',
            sort:'calc_totalDue_sum:asc',
            format:'xlsx'
        ]
        controller.list()

        then:
        response.status == 200
        response.header("Content-Type").contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }



}
