package testing

import gorm.tools.databinding.EntityMapBinder
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink

@Integration
@Rollback
class EntityMapBinderSpec extends Specification {
    EntityMapBinder entityMapBinder

    @Issue("https://github.com/yakworks/gorm-tools/issues/181")
    @Ignore
    void "perform gormtools binding after grails binding"() {
        setup:
        Map params = [num: "123", name:"test-org", type: "Customer", location:[city:"Rajkot"]]

        when: "Stuff is bound as part of org association binding"
        Org org = new Org()
        org.properties = params

        then:
        org.hasErrors() == false
        org.name == "test-org"
        org.location != null
        org.location.city == "Rajkot"

        when: "now try to bind just address"
        Location address = new Location()
        address.bind params.location

        then:
        address != null
        address.city == "Rajkot"
    }

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

    void "bind: domain with composite id"() {
        given:
        Tag t = new Tag(name:"test")

        when:
        TagLink link = new TagLink()
        entityMapBinder.bind([:], link, [tag:t, linkedId:"1", linkedEntity: "Org"])

        then:
        link.tag == t
        link.linkedId == 1
        link.linkedEntity == "Org"
    }
}
