package yakworks.rally.mail.mailgun

import java.nio.file.Paths
import java.time.ZonedDateTime

import org.springframework.beans.factory.annotation.Autowired

import com.mailgun.api.v4.MailgunEmailVerificationApi
import com.mailgun.client.MailgunClient
import com.mailgun.model.events.EventsQueryOptions
import com.mailgun.model.events.EventsResponse
import com.mailgun.model.message.Message
import com.mailgun.model.message.MessageResponse
import com.mailgun.model.verification.AddressValidationResponse
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.api.Result
import yakworks.commons.util.BuildSupport
import yakworks.rally.mail.MailTo
import yakworks.rally.mail.config.MailProps
import yakworks.testing.gorm.integration.DataIntegrationTest

/**
 * This sends real emails so dont want it running every time test suite is run
 * add the app.mail.mailgun.private-api-key to test/resources/application.yml and these will work
 */
@Ignore
@Integration
@Rollback
class MailgunServiceTest extends Specification implements DataIntegrationTest  {

    @Autowired MailgunService emailService
    //@Autowired MailgunEventsApi mailgunEventsApi
    @Autowired MailProps mailConfig

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

    def "sanity check config"() {
        expect:
        emailService
        emailService.mailgunEventsApi
        emailService.mailgunMessagesApi
        //mailgunEventsApi
        mailConfig.enabled
        mailConfig.defaultDomain
        mailConfig.mailgun.privateApiKey
        mailConfig.mailgun.enabled
    }

    //@Ignore
    def "simple with min required fields"() {
        when:
        MailTo mailMsg = new MailTo(
            from: "9ci Account Services <rndc@greenbill.io>",
            to: ["josh2@9ci.com"],
            text: 'Testing',
            //subject can be empty but obviously not recomended
            subject: "Testing",
        )

        Result res = emailService.send(mailMsg)

        then:
        res.ok
    }

    @Ignore //comment this out and change email to test. still need to sort out how to test this without sending email
    def "MailService html email with attachments"() {
        when:
        def testAttach = Paths.get(BuildSupport.rootProjectDir, 'examples/resources/test.txt')
        MailTo mailMsg = new MailTo(
            from: "Yakworks Account Services <rndc@greenbill.io>",
            replyTo: "billing@rndc.com",
            //to: ["josh2@9ci.com, joshua@9ci.com"],
            to: ['josh2@9ci.com, "Blow, Joe" <joshua@9ci.com>'],
            subject: "RNDC Test Statement xx",
            tags: ["statements"],
            html: htmlBody,
            attachments: [testAttach.toFile()]
        )

        Result res = emailService.send(mailMsg)
        //Response messageResponse = mailgunService.mailgunMessagesApi.sendMessageFeignResponse(DOMAIN, message)
        then:
        res.ok
    }

    @Ignore //comment this out and change email to test. still need to sort out how to test this without sending email
    def "DIRECT mailgun api example html email with attachments"() {
        when:
        def testAttach = Paths.get(BuildSupport.rootProjectDir, 'examples/resources/test.txt')
        Message message = Message.builder()
            .from("RNDC Account Services <rndc@greenbill.io>")
            .replyTo("billing@rndc.com")
            //.to(["joshua@9ci.com","Kuhn, John <jkuhn@9ci.com>", 'Dabal, Joanna <joanna@9ci.com>'])
            .to('josh2@9ci.com, "Blow, Joe" <joshua@9ci.com>')
            .subject("RNDC Test Statement")
            //.text("foo") //text only
            .tag("statements") //sets and o:tag to segregate
            .html(htmlBody)
            .attachment(testAttach.toFile())
            .build()
        MessageResponse messageResponse = emailService.sendMessage(mailConfig.defaultDomain, message)

        then:
        messageResponse
        //<20230215133923.f0c534fc3f7d5215@m.greenbill.io>
    }

    def "missing from "() {
        when:
        MailTo mailMsg = new MailTo(
            //from: "Yakworks Account Services <rndc@greenbill.io>",
            to: ["josh2@9ci.com"],
            text: 'foo'
        )

        Result res = emailService.send(mailMsg)

        then:
        !res.ok
        res.detail == "Field 'from' cannot be null or empty!"
    }

    def "MailService html bad to format"() {
        when:
        MailTo mailMsg = new MailTo(
            from: "RNDC Account Services <rndc@greenbill.io>",
            replyTo: "billing@rndc.com",
            to: ["josh2@9ci.com, adfasdfasdf"],
            subject: "SHOULD FAIL",
            html: htmlBody
        )

        Result res = emailService.send(mailMsg)
        //Response messageResponse = mailgunService.mailgunMessagesApi.sendMessageFeignResponse(DOMAIN, message)
        then:
        !res.ok
        res.detail.contains("not a valid address")
        res.status.code == 400
    }

    def "MailService bad domain in mailgun"() {
        when:
        MailTo mailMsg = new MailTo(
            from: "RNDC Account Services <rndc@greenbill.io>",
            replyTo: "billing@rndc.com",
            to: ["josh2@9ci.com, adfasdfasdf"],
            subject: "SHOULD FAIL",
            html: htmlBody
        )

        Result res = emailService.send("bad-domain.com", mailMsg)
        //Response messageResponse = mailgunService.mailgunMessagesApi.sendMessageFeignResponse(DOMAIN, message)
        then:
        !res.ok
        res.title.contains("Unauth")
        res.status.code == 401
    }

    def "get all events"() {
        when:
        EventsResponse result = emailService.getEvents()

        then:
        result
        result.items
        result.paging
    }

    def "get all delivered events from lat 24 hours"() {
        when:
        EventsQueryOptions eventsQueryOptions = EventsQueryOptions.builder()
            //.event(EventType.DELIVERED)
            .begin(ZonedDateTime.now())
            .end(ZonedDateTime.now().minusDays(1))
            .build();

        EventsResponse result = emailService.getEvents()

        then:
        result
        result.items
        result.paging
    }

    def "Validation"() {
        when:
        MailgunEmailVerificationApi mgunVerify = MailgunClient.config(mailConfig.mailgun.privateApiKey)
            .createApi(MailgunEmailVerificationApi.class)

        AddressValidationResponse result = mgunVerify.validateAddress("canderson@marczyk.com")

        then:
        result.result == "undeliverable" || result.result == "do_not_send"
        result.reason
    }

}
