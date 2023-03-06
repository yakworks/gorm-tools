package yakworks.rally.activity


import org.springframework.beans.factory.annotation.Autowired

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.api.Result
import yakworks.rally.activity.model.Activity
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.mail.testing.TestMailService
import yakworks.rally.testing.MockData
import yakworks.testing.gorm.integration.DomainIntTest

import static yakworks.rally.activity.model.Activity.Kind as ActKind

@Integration
@Rollback
class ActivityServiceTests extends Specification implements DomainIntTest {
    @Autowired ActivityService activityService
    @Autowired TestMailService emailService

    void "buildEmail"() {
        setup:
        MailMessage mailMsg = MockData.mailMessage()
        flushAndClear()
        when:
        Activity act = activityService.buildEmail(9, mailMsg)
        //flush()

        then:
        act != null
        act.name == "Test Email"
        act.kind == ActKind.Email

    }

    void "sendEmail"() {
        setup:
        MailMessage mailMsg = MockData.mailMessage()
        Activity act = activityService.buildEmail(9, mailMsg)
        act.persist()
        flushAndClear()

        when:
        Result res = activityService.sendEmail(act.id)
        Activity sentAct = Activity.get(act.id)

        then: "sanity check act"
        res.ok
        sentAct
        sentAct.name == "Test Email"
        sentAct.kind == ActKind.Email

        and: "check the testMailService"
        emailService.sentMail.size() == 1
        sentAct.mailMessage.state == MailMessage.MsgState.Sent
    }

}
