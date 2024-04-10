package yakworks.rally.mail.services

import org.springframework.beans.factory.annotation.Autowired

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.mail.model.ContentType
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.mail.model.MailerTemplate
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class CommonMailerTests extends Specification implements DomainIntTest {

    @Autowired CommonMailer commonMailer

    void "test createMailMessage"() {
        when:
        MailerTemplate receipt = new MailerTemplate(
            sendFrom: "bob@bill.com",
            replyTo: "Test Account <joe@greenbill.io>",
            subject: 'Payment Receipt',
            body: '''
                ID: {{ payTran.id }}
                Amount: {{ numberFormat payTran.amount "currency" }}
            '''.stripLeading(),
            tags: ['ipay']
        )
        Map model = [payTran: [id: 123, amount: 9999.9]]
        MailMessage mailMsg = commonMailer.createMailMessage("admin@testmeee.com", receipt, model)

        then:
        mailMsg
        mailMsg.sendFrom == "bob@bill.com"
        mailMsg.replyTo == "Test Account <joe@greenbill.io>"
        mailMsg.subject == "Payment Receipt"
        mailMsg.sendTo == "admin@testmeee.com"
        mailMsg.contentType == ContentType.plain

        when:
        //sanity check the body
        String body = mailMsg.body

        then:
        body.contains("ID: 123")
        body.contains("Amount: \$9,999.90")

        when: "content type is html"
        receipt.body = "<td>ID</td><td>{{  payTran.id }}</td>"
        receipt.contentType = ContentType.html
        mailMsg = commonMailer.createMailMessage("admin@testmeee.com", receipt, model)

        then:
        mailMsg.contentType == ContentType.html
        mailMsg.body.contains "<td>ID</td><td>123</td>"
    }

}
