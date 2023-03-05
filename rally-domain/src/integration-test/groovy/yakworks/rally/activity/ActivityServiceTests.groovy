package yakworks.rally.activity

import java.nio.file.Files

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.mail.MailService
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.mail.testing.TestMailService
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag
import yakworks.rally.testing.MockData
import yakworks.testing.gorm.integration.DomainIntTest

import static yakworks.rally.activity.model.Activity.Kind as ActKind
import static yakworks.rally.activity.model.Activity.VisibleTo

@Integration
@Rollback
class ActivityServiceTests extends Specification implements DomainIntTest {
    @Autowired ActivityService activityService
    @Autowired TestMailService mailService

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
        Activity sentAct = activityService.sendEmail(act.id)

        then: "sanity check act"
        sentAct
        sentAct.name == "Test Email"
        sentAct.kind == ActKind.Email

        and: "check the testMailService"
        mailService.sentMail.size() == 1
        sentAct.mailMessage.state == MailMessage.MsgState.Sent
    }

}
