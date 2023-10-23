package yakworks.rally.mail

import spock.lang.Specification
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.testing.MockData
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

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
