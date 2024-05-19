package yakworks.rally.tag

import spock.lang.Specification
import yakworks.rally.tag.model.Tag
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest
import yakworks.testing.gorm.unit.DataRepoTest

class TagSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [Tag]

    void "test isValidFor"() {
        when:
        def tag2 = Tag.create(name: 'tag2', code: 'tag2', entityName: 'Foo, Attachment')
        flush()

        then:
        tag2.isValidFor('Foo')
        tag2.isValidFor('Attachment')
        !tag2.isValidFor('Bar')

    }

}
