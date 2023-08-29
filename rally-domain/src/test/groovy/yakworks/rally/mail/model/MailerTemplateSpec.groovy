package yakworks.rally.mail.model

import java.nio.file.Path
import javax.mail.internet.InternetAddress

import spock.lang.Specification
import yakworks.api.problem.ThrowableProblem
import yakworks.rally.mail.EmailUtils

class MailerTemplateSpec extends Specification {

    void "test clone"() {
        when:
        var mt = new MailerTemplate(
            sendFrom: "bob@bob.com",
            bodyTemplate: Path.of("foo"),
            tags: ['tag1'],
            attachmentIds: [1L, 2L]
        )
        var mtc = mt.clone()

        then:
        mtc != mt
        !mtc.is(mt)

        mtc.bodyTemplate == mt.bodyTemplate

        // Change property on cloned instance.
        mtc.attachmentIds.add(3L)
        mtc.attachmentIds == [1L, 2L, 3L]
        mt.attachmentIds == [1L, 2L]

    }

}
