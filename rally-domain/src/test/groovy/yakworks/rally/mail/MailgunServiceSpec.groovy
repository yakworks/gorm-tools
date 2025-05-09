package yakworks.rally.mail

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.model.message.MessageResponse
import spock.lang.Specification
import yakworks.api.Result
import yakworks.rally.mail.mailgun.MailgunService
import feign.FeignException
import yakworks.rally.mail.EmailService.MailTo

class MailgunServiceSpec extends Specification {

    void "send with retry"() {
        setup:
        MailgunService mailgunService = new MailgunService()
        mailgunService._mailgunMessagesApi = Mock(MailgunMessagesApi)
        int attempt = 1

        when:
        MailTo mailTo = new MailTo(from:"test@9ci.com", to: ["test@9ci.com"], subject: "test", text: "test")
        Result result = mailgunService.send("test", mailTo)

        then:
        //should be tried twice and 2nd attempt would succeed
        2 * mailgunService._mailgunMessagesApi.sendMessage(_, _) >> {
            if(attempt < 2) {
                attempt++
                throw new FeignException(500, "test")
            }
            else {
                return new MessageResponse("test", "test")
            }
        }
        attempt == 2
        result.ok
    }
}
