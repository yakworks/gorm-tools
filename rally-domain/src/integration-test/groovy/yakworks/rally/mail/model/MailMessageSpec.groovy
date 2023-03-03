package yakworks.rally.mail.model

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class MailMessageSpec extends Specification implements DomainIntTest {

    String htmlBody = """
        <html><body>
            <p style="color:blue; font-size:30px;">Statement</p>
            <p style="color:blue; font-size:12px;">
                This is a statement of your account balance with RNDC.
                <a style="color: #8798AD;margin: 0;text-decoration: none;" href="%tag_unsubscribe_url%">Unsubscribe</a>
                to stop recieving statements. If you wish to deliver staments to a different email address then please
                contact your customer service representitive and do not unsubscribe.
            </p>
        </body></html>
    """

    void "test create"() {
        when:
        def msg = new MailMessage(
            state: MailMessage.MsgState.Queued,
            sendTo: 'josh2@9ci.com, "Blow, Joe" <joshua@9ci.com>',
            sendFrom: "Yakworks Account Services <rndc@greenbill.io>",
            replyTo: "billing@rndc.com",
            subject: "RNDC Test Statement xx",
            tags: ["statements", "misc"],
            body: htmlBody,
            attachmentIds: [1,2,3]
        ).persist()
        flushAndClear()
        def msg2 = MailMessage.get(msg.id)
        msg2.source = "foo"
        msg2.persist(flush: true)

        then:
        msg
        msg2.createdBy
        msg2.createdDate
        msg2.sendTo
        msg2.sendTo
        msg2.sendFrom
        msg2.replyTo
        msg2.subject
        msg2.body
        msg2.attachmentIds.size() == 3
        msg2.tags.size() == 2
    }

}
