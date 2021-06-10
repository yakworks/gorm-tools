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

    void "test populate name from code"() {
        when:
        def tag = Tag.create('code': 'a-b', entityName: 'Attachment')

        then:
        tag.validate()
        'a b' == tag.name
        'a-b' == tag.code
    }

    void "test code regex"() {
        when:
        def tag = Tag.create('code': 'aA12-_', name: 'tag', entityName: 'Attachment')

        then:
        tag.validate()

        when:
        tag.code = 'aA 12-_'

        then:
        !tag.validate()

        when:
        tag.code = 'aA12-_*()'

        then:
        !tag.validate()

        when:
        tag.code = '1234567890A'

        then:
        !tag.validate()
    }

}
