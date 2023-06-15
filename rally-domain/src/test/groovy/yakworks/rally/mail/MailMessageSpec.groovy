package yakworks.rally.mail

import org.apache.commons.lang3.RandomStringUtils

import gorm.tools.model.Persistable
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.TaskType
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink
import yakworks.rally.testing.MockData
import yakworks.spring.AppResourceLoader
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

import static yakworks.rally.activity.model.Activity.Kind as ActKinds

class MailMessageSpec extends Specification implements GormHibernateTest, SecurityTest {
    static entityClasses = [
        MailMessage
    ]

    void "smoke test"() {
        when:
        MailMessage mm = MockData.mailMessage()

        then:
        mm
        // !mm.validate()
        // mm.errors.allErrors.size() == 2
        //mm.errors['name'].code == 'MaxLength'

    }

}
