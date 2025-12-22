package yakworks.rally.mail.model

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Specification
import yakworks.rally.testing.MockData
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class MailMessageSpec extends Specification implements DomainIntTest {

    // String htmlBody = """
    //     <html><body>
    //         <p style="color:blue; font-size:30px;">Statement</p>
    //         <p style="color:blue; font-size:12px;">
    //             This is a statement of your account balance with RNDC.
    //             <a style="color: #8798AD;margin: 0;text-decoration: none;" href="%tag_unsubscribe_url%">Unsubscribe</a>
    //             to stop recieving statements. If you wish to deliver staments to a different email address then please
    //             contact your customer service representitive and do not unsubscribe.
    //         </p>
    //     </body></html>
    // """

    void "test create"() {
        when:
        def msg = MockData.mailMessage()
        msg.attachmentIds = [1,2,3]
        msg.msgResponse = RandomStringUtils.random(2000, "abcde")
        msg.persist()
        flush()

        def msg2 = MailMessage.get(msg.id)
        msg2.source = "foo"
        msg2.persist()
        flushAndClear()

        then:
        noExceptionThrown()
        msg
        msg.msgResponse.length() == 2000
        msg2.createdBy
        msg2.createdDate
        msg2.sendTo
        msg2.sendTo
        msg2.sendFrom
        msg2.replyTo
        msg2.subject
        msg2.body
        msg2.attachmentIds.size() == 3
        msg2.tags.size() == 1
    }

    void "large mail message size success"() {
        String body = RandomStringUtils.randomAlphanumeric(100000)
        def msg = new MailMessage(
            state: MailMessage.MsgState.Queued,
            sendTo: 'test@9ci.com',
            sendFrom: "test@9ci.com",
            replyTo: "test@9ci.com",
            subject: "test",
            body: body,
        )

        when:
        msg.persist()
        flushAndClear()

        then:
        noExceptionThrown()

        when:
        def msg2 = MailMessage.get(msg.id)

        then:
        msg2.body.length() == 100000
    }

}
