package yakworks.rest

import gorm.tools.transaction.WithTrx
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import spock.lang.Shared
import spock.lang.Specification
import yakworks.rally.orgs.model.PartitionOrg
import yakworks.rest.client.OkHttpRestTrait

@Integration
class PartitionOrgRestApiSpec  extends Specification implements OkHttpRestTrait, WithTrx  {

    String org_path = "/api/rally/org"
    String path = "/api/rally/partitionOrg"

    @Shared Long id

    def setup(){
        login()
    }

    void "create through org"() {
        when:
        Response resp = post(org_path, [num: "foobie123", name: "foobie", type: "Company"])
        Map body = bodyToMap(resp)
        id = body.id as Long

        then:
        id
        body.id
        PartitionOrg.repo.getWithTrx(id)
    }

    void "GET"() {
        when:
        Response resp = get(path+"/$id")
        Map body = bodyToMap(resp)

        then:
        body
        body.id
        body.name == "foobie"
    }

    void "create is disabled"() {
        when:
        Response resp = post(path, [num: "foobie123", name: "foobie"])
        Map body = bodyToMap(resp)

        then:
        !body.ok
        body.detail == 'Can not create Partition org'
    }

    void "update is disabled"() {
        when:
        Response resp = put(path+"/$id", [num: "foobie123", name: "foobie"])
        Map body = bodyToMap(resp)

        then:
        !body.ok
        body.detail == 'Can not update Partition org'
    }

    void "delete is disabled"() {
        when:
        Response resp = delete(path+"/$id")
        Map body = bodyToMap(resp)

        then:
        !body.ok
        body.detail == 'Can not delete Partition org'
    }
}
