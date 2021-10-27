package yakworks.rally.tag

import yakworks.gorm.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink

class TagLinkSpec extends Specification implements DomainRepoTest<TagLink>, SecurityTest {

    void setupSpec() {
        mockDomains Tag, Attachment
    }

    List createSomeTags(){
        def tag1 = Tag.create(name: 'tag1', code: 'tag1')
        def tag2 = Tag.create(name: 'tag2', code: 'tag2', entityName: 'Foo, Attachment')
        def tag3 = Tag.create(name: 'tag3', code: 'tag3', entityName: 'Attachment')
        def tag4 = Tag.create(name: 'tag4 wont work', code: 'tag4', entityName: 'SomethingElse')
        return [tag1, tag2, tag3, tag4]
    }

    Attachment setupAnAttachmentWithTags(){
        def tags = createSomeTags()
        def tagId1 = tags[0].id
        def tagId2 = tags[0].id

        def attachData = [
            name: 'foo.txt',
            tags: [[id: 1], [id: 2], [id: 3]]
        ]
        return Attachment.create(attachData)
    }

    void "sanity check"() {
        when:
        def att = setupAnAttachmentWithTags()

        then:
        att.hasTags()
        att.getTags().size() == 3
    }

    void "create link"() {
        when:
        def tag1 = Tag.create(name: 'tag1', code: 'tag1')
        def tag2 = Tag.create(name: 'tag2', code: 'tag2', entityName: 'Foo, Attachment')
        def att = Attachment.create(name: 'foo.txt')
        def attId = att.id

        def tl = TagLink.create(att, tag1)
        def tl2 = TagLink.create(att, tag2)
        assert tl.tag == tag1
        assert tl.linkedId == att.id
        //flushAndClear()

        then:
        TagLink.exists(att, tag1)
        TagLink.exists(att, tag2)

        def list = TagLink.list(att)
        list.size() == 2
        def link = list[0]
        link.tag.id == tag1.id
        link.linkedId == attId
        link.linkedEntity == 'Attachment'

        def tagList = TagLink.listTags(att)
        tagList.size() == 2
        tag1 == tagList[0]
        tag2 == tagList[1]

        def link2 = TagLink.get(att, tag1)
        link2.tag == tag1
        link2.linkedId == att.id
        link2.linkedEntity == 'Attachment'

    }

    void "create an attachment with tags"() {
        when:
        def tag1 = Tag.create(name: 'tag1', code:'tag1', entityName: 'Something')
        def att = new Attachment(name: 'foo', location: 'foo').persist()

        def tl = TagLink.create(att, tag1)

        then:
        thrown IllegalArgumentException

    }

    void "create link with invalid tag"() {
        when: 'tag not valid for entity'
        def tag1 = Tag.create(name: 'tag1', code:'tag1', entityName: 'Something')
        def att = new Attachment(name: 'foo', location: 'foo').persist()

        def tl = TagLink.create(att, tag1)

        then:
        thrown IllegalArgumentException

    }

    @Ignore //not working in unit tests
    void "create link duplicate"() {
        when: 'tag not valid for entity'
        def tag1 = Tag.create(name: 'tag1', code: 'tag1')
        def att = new Attachment(name: 'foo', location: 'foo').persist()

        TagLink.create(att, tag1, [flush:true])
        TagLink.create(att, tag1, [flush:true]) //should break when attempted again
        flushAndClear()

        then:
        thrown IllegalArgumentException
    }
}
