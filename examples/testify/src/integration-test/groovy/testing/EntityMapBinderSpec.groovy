package testing

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.databinding.EntityMapBinder
import gorm.tools.repository.RepoUtil
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.orgs.model.Org

@Integration
@Rollback
class EntityMapBinderSpec extends Specification {

    @Autowired EntityMapBinder binder

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

    void "association with uuid"() {
        setup:
        MailMessage msg = MailMessage.create(state: MailMessage.MsgState.Queued, source: "test", sendFrom:"test@9ci.cim", sendTo:"dev@9ci.com")
        RepoUtil.flush()

        expect:
        msg.id
        msg.id instanceof UUID

        when: "using the Id suffix"
        Activity activity = new Activity()
        binder.bind(activity, [mailMessageId:msg.id.toString()])

        then: "should convert and bind it"
        noExceptionThrown()
        activity.mailMessage
        activity.mailMessage.id == msg.id
        !activity.hasErrors()

        when: "id is in the map"
        activity = new Activity()
        binder.bind(activity, [mailMessage:[id:msg.id.toString()]])

        then:
        noExceptionThrown()
        activity.mailMessage
        activity.mailMessage.id == msg.id
        !activity.hasErrors()
    }
}
