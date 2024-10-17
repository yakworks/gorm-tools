package yakworks.rest

import grails.testing.mixin.integration.Integration
import okhttp3.Response
import org.springframework.http.HttpStatus
import spock.lang.Specification
import yakworks.commons.lang.IsoDateUtil
import yakworks.rally.activity.model.Activity

import yakworks.rally.orgs.model.Org
import yakworks.rest.client.OkHttpRestTrait

import java.time.LocalDateTime

@Integration
class ActivityRestApiSpec  extends Specification implements OkHttpRestTrait {

    def setup(){
        login()
    }

    void "sanity check create"() {
        when:
        LocalDateTime now = LocalDateTime.now()
        Response resp = post("/api/rally/activity", [org:[id: 2], name: "test-note", actDate:now])
        Map body = bodyToMap(resp)

        then:
        resp.code() == HttpStatus.CREATED.value()
        body.name == "test-note"
        body.org.id == 2
        body.kind == 'Note'
        body.actDate == IsoDateUtil.format(now)
    }

    void "actDate is not updateable"() {
        setup:
        Activity act
        Activity.withNewTransaction {
            act = Activity.create([org: Org.load(10), note: [body: 'Test note']])
        }

        expect:
        act
        act.id
        act.actDate

        when:
        Response resp = put("/api/rally/activity/$act.id", [id:act.id, actDate: LocalDateTime.now()])
        Map body = bodyToMap(resp)

        then:
        resp.code() == 422
        !body.ok
        body.errors
        body.errors.size() == 1
        body.errors[0].code == "error.notupdateable"
        body.errors[0].field == "actDate"

        cleanup:
        if(act) {
            Activity.withNewTransaction {
                Activity.repo.removeById(act.id)
            }
        }
    }
}
