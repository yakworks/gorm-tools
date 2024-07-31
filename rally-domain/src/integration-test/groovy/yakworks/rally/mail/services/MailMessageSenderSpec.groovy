package yakworks.rally.mail.services

import org.springframework.beans.factory.annotation.Autowired

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.api.Result
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.mail.EmailService
import yakworks.rally.mail.EmailService.MailTo
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.testing.MockData
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class MailMessageSenderSpec extends Specification implements DomainIntTest {

    @Autowired MailMessageSender mailMessageSender
    @Autowired EmailService emailService

    void setup() {
        emailService.sentMail = []
    }

    MailMessage mailMessageWithAttachment(){
        Map params = [
            name:'hello.txt', subject:'greetings', bytes: 'blah blah blah'.getBytes()
        ]
        Attachment attachment = Attachment.repo.create(params)
        MailMessage mailMsg = MockData.mailMessage()
        mailMsg.attachmentIds = [attachment.id]
        mailMsg.persist()
        flushAndClear()
        return mailMsg
    }

    void "smoke test mail message"() {
        when:
        //assert emailService instanceof MailgunService
        MailMessage mailMsg = MockData.mailMessage()
        flushAndClear()
        assert MailMessage.get(mailMsg.id)

        then:
        mailMsg
    }

    void "convertMailMessage"() {
        when:
        MailMessage mailMsg = MockData.mailMessage()
        //mailMsg.attachmentIds = [1,2,3]
        //mailMsg.persist()
        flushAndClear()
        MailTo mailTo = mailMessageSender.convertMailMessage(mailMsg)

        then:
        mailTo.to[0] == mailMsg.sendTo
        mailTo.from == mailMsg.sendFrom
        mailTo.replyTo == mailMsg.replyTo
        mailTo.subject == mailMsg.subject
        mailTo.getText() == mailMsg.body
        mailTo.tags == mailMsg.tags

    }

    void "convertMailMessage with attachment"() {
        setup:
        MailMessage mailMsg = mailMessageWithAttachment()

        when:
        MailTo mailTo = mailMessageSender.convertMailMessage(mailMsg)

        then:
        mailTo
        mailTo.attachments.size() == 1
    }

    void "convertMailMessage bad attachment"() {
        when:
        MailMessage mailMsg = MockData.mailMessage()
        mailMsg.attachmentIds = [1]
        mailMsg.persist()
        flushAndClear()
        mailMessageSender.convertMailMessage(mailMsg)

        then:
        thrown(IllegalArgumentException)
    }

    void "send"() {
        when:
        MailMessage mailMsg = MockData.mailMessage()
        flush()
        mailMessageSender.send(mailMsg)

        then:
        mailMsg.state == MailMessage.MsgState.Sent
        mailMsg.messageId
        emailService.sentMail.size() == 1
    }

    void "send : bad email"() {
        when:
        MailMessage mailMsg = MockData.mailMessage()
        mailMsg.sendTo = mailMsg.sendTo +  ', "jon Doe" <john@doe.com>,test@9ci.com.'
        flush()
        mailMessageSender.send(mailMsg)

        then:
        mailMsg.state == MailMessage.MsgState.Error
        mailMsg.msgResponse.contains("Invalid email address : test@9ci.com.")
        emailService.sentMail.size() == 0
    }

    void "send with attachment"() {
        when:
        MailMessage mailMsg = mailMessageWithAttachment()
        flush()
        mailMessageSender.send(mailMsg)

        then:
        mailMsg.state == MailMessage.MsgState.Sent
        mailMsg.messageId
        emailService.sentMail.size() == 1
    }

    void "send with bad email"() {
        when:
        MailMessage mailMsg = mailMessageWithAttachment()
        mailMsg.sendTo = "bad@email.com"
        mailMsg.persist(flush: true)

        Result res = mailMessageSender.send(mailMsg)
        flushAndClear()
        mailMsg.refresh()

        then:
        emailService.sentMail.size() == 0
        !res.ok
        !mailMsg.messageId
        mailMsg.state == MailMessage.MsgState.Error
        mailMsg.msgResponse == "bad mail"
    }

    void "send with bad attachment"() {
        when:
        MailMessage mailMsg = mailMessageWithAttachment()
        mailMsg.attachmentIds.add(1)
        mailMsg.persist(flush: true)

        Result res = mailMessageSender.send(mailMsg)
        flushAndClear()
        mailMsg.refresh()

        then:
        emailService.sentMail.size() == 0
        !res.ok
        !mailMsg.messageId
        mailMsg.state == MailMessage.MsgState.Error
        mailMsg.msgResponse.contains("does not exist")
    }

    void "validate addresses"() {
        expect: "valid"
        emailService.validateEmail("test@test.com").ok
        emailService.validateEmail('"John Doe" <test@test.com>').ok
        emailService.validateEmail('"John, Doe" <test@test.com>').ok
        emailService.validateEmail('one@one.com, two@two.om').ok
        emailService.validateEmail('"One one" <one@one.com>, "Two, two" <two@two.com>').ok

        and: "invalid"
        !emailService.validateEmail("test@test.com.").ok
        !emailService.validateEmail('John Doe').ok

        when:
        Result r = emailService.validateEmail('"John, Doe" <test@test.com>,test@test.com.')

        then: "reports invalid email in detail"
        !r.ok
        r.detail == "Invalid email address : test@test.com."
    }

}
