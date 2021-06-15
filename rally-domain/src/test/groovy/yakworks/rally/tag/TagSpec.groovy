package yakworks.rally.tag


import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification
import yakworks.rally.tag.model.Tag

class TagSpec extends Specification implements DomainRepoTest<Tag>, SecurityTest {

    // void setupSpec() {
    //     mockDomains Tag, Attachment
    // }

    void "test isValidFor"() {
        when:
        def tag2 = Tag.create(name: 'tag2', code: 'tag2', entityName: 'Foo, Attachment')

        then:
        tag2.isValidFor('Foo')
        tag2.isValidFor('Attachment')
        !tag2.isValidFor('Bar')

    }

}
