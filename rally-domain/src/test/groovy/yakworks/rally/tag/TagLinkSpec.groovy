package yakworks.rally.tag

import spock.lang.Specification
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink
import yakworks.testing.gorm.unit.SecurityTest
import yakworks.testing.gorm.unit.DataRepoTest

class TagLinkSpec extends Specification implements DataRepoTest, SecurityTest {
    static List entityClasses = [TagLink, Tag, Attachment]

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

    void "test add remove - list"() {
        setup:
        Attachment att = new Attachment(name: 'foo', location: 'foo').persist()
        Tag t1 = Tag.create(name: 't1', code:'t1', entityName: 'Attachment')
        Tag t2 = Tag.create(name: 't2', code:'t2', entityName: 'Attachment')
        Tag t3 = Tag.create(name: 't3', code:'t3', entityName: 'Attachment')

        when:
        TagLink.addOrRemoveTags(att, [[id:t1.id], [id:t2.id]])

        then:
        TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        !TagLink.exists(att, t3)

        when:
        TagLink.addOrRemoveTags(att, [[id:t2.id], [id:t3.id]])
        flushAndClear()

        then:
        TagLink.count() == 2
        !TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        TagLink.exists(att, t3)
        !att.tags.contains(t1)
    }

    void "test add remove - loose json string "() {
        setup:
        Attachment att = new Attachment(name: 'foo', location: 'foo').persist()
        Tag t1 = Tag.create(name: 't1', code:'t1', entityName: 'Attachment')
        Tag t2 = Tag.create(name: 't2', code:'t2', entityName: 'Attachment')
        Tag t3 = Tag.create(name: 't3', code:'t3', entityName: 'Attachment')

        when:
        TagLink.addOrRemoveTags(att, '[{id:1}, {id:2}]')

        then:
        TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        !TagLink.exists(att, t3)

        when:
        TagLink.addOrRemoveTags(att, '[{id:2}, {id:3}]')
        flushAndClear()

        then:
        TagLink.count() == 2
        !TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        TagLink.exists(att, t3)
        !att.tags.contains(t1)
    }

}
