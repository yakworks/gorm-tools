package yakworks.rally.common

import yakworks.gorm.testing.SecurityTest
import yakworks.gorm.testing.unit.DomainRepoTest
import spock.lang.Specification
import yakworks.rally.tag.model.Tag

/**
 * uses Tag to test the trait
 */
class NameCodeDescriptionSpec extends Specification  implements DomainRepoTest<Tag>, SecurityTest  {

    void "test populate name from code"() {
        when: 'no name, so take it from code'
        def tag = new Tag(code: 'a-b', entityName: 'Attachment')

        then: 'name populated, dash replaced with space'
        tag.validate()
        'a b' == tag.name
        'a-b' == tag.code
    }

    void "test no code or name"() {
        when: 'no code so cannot be created'
        def tag = new Tag(entityName: 'Attachment')

        then: 'should fail'
        !tag.validate()
    }

    void "code regex validation should succeed"() {
        when:
        def tag = Tag.create('code': 'aA12-_', name: 'tag', entityName: 'Attachment')

        then:
        tag.validate()

    }
    void "code regex validation should fail"() {
        setup:
        def tag = Tag.create('code': 'code', name: 'name', entityName: 'Attachment')

        when: "code has space"
        tag.code = 'aA 12-_'

        then: "should fail"
        !tag.validate()

        when: "code has special chars other than _ & -"
        tag.code = 'aA12-_*()'

        then: "should fail"
        !tag.validate()

        when: "code is longer than 10 chars"
        tag.code = '1234567890A'

        then: "should fail"
        !tag.validate()
    }


}
