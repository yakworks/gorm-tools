package testing

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org

@Integration
@Rollback
class EntityMapBinderSpec extends Specification {

    void "test bindable : should create new instance"() {
        given:
        Map params = [ num: "123", name: "Wirgin", type: "Customer", info: [phone: "1-800"]]

        when:
        Org p = new Org()
        p.bind params

        then:
        p.name == "Wirgin"
        p.info != null
        p.info.phone == "1-800"
    }

    void "should update existing associated instance when bindable"() {
        given:
        Map params = [ num: "123", name: "Wirgin", type: "Customer", info: [phone: "1-800"]]
        Org org = Org.create(params)
        Org.repo.flushAndClear()

        when:
        def org2 = Org.get(org.id)

        then:
        org2 != null

        when:
        params = [info: [phone: "1-900"]]
        org2.bind params

        then:
        org2.info.phone == "1-900"
    }
}
