package yakworks.rally.tag

import spock.lang.Specification
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest
import yakworks.testing.gorm.unit.DataRepoTest

class TagLinkSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [TagLink, Tag, Attachment]

    List createSomeTags(){
        def tag1 = Tag.repo.create([id: 1, name: 'tag1', code: 'tag1'], [bindId: true])
        def tag2 = Tag.repo.create([id: 2, name: 'tag2', code: 'tag2', entityName: 'Foo, Attachment'], [bindId: true])
        def tag3 = Tag.repo.create([id: 3, name: 'tag3', code: 'tag3', entityName: 'Attachment'], [bindId: true])
        def tag4 = Tag.repo.create([id: 4, name: 'tag4 wont work', code: 'tag4', entityName: 'SomethingElse'], [bindId: true])
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
        def achm = Attachment.create(attachData)
        flushAndClear()
        return achm
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
        flushAndClear()

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

    void "test add remove - replace is the default"() {
        setup:
        Attachment att = new Attachment(name: 'foo', location: 'foo').persist()
        Tag t1 = Tag.create(name: 't1', code:'t1', entityName: 'Attachment')
        Tag t2 = Tag.create(name: 't2', code:'t2', entityName: 'Attachment')
        Tag t3 = Tag.create(name: 't3', code:'t3', entityName: 'Attachment')

        when: "add t1 t2"
        TagLink.addOrRemoveTags(att, [[id:t1.id], [id:t2.id]])
        flushAndClear()

        then:
        TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        !TagLink.exists(att, t3)

        when: "remove t1 add t3"
        TagLink.addOrRemoveTags(att, [[id:t2.id], [id:t3.id]])
        flushAndClear()

        then:
        TagLink.count() == 2
        !TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        TagLink.exists(att, t3)
        !att.tags.contains(t1)

        when: "no change"
        TagLink.addOrRemoveTags(att, [[id:t2.id], [id:t3.id]])

        then:
        TagLink.count() == 2
        !TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        TagLink.exists(att, t3)
        !att.tags.contains(t1)

        when: "null"
        TagLink.addOrRemoveTags(att, null)

        then: "Should do nothing"
        TagLink.count() == 2

        when: "Empty"
        TagLink.addOrRemoveTags(att, [])

        then: "Should remove all"
        TagLink.count() == 0
    }

    void "test add remove - map with data ops"() {
        setup:
        Attachment att = new Attachment(name: 'foo', location: 'foo').persist()
        Tag t1 = Tag.create(name: 't1', code:'t1', entityName: 'Attachment')
        Tag t2 = Tag.create(name: 't2', code:'t2', entityName: 'Attachment')
        Tag t3 = Tag.create(name: 't3', code:'t3', entityName: 'Attachment')

        when: "add t1 t2"
        TagLink.addOrRemoveTags(att, [[id:t1.id], [id:t2.id]])
        flushAndClear()

        then:
        TagLink.count() == 2
        TagLink.exists(att, t1)
        TagLink.exists(att, t2)

        when: "Add one more"
        TagLink.addOrRemoveTags(att, [op:"update", data:[[id:t3.id]]])
        flushAndClear()
        then:
        TagLink.count() == 3
        TagLink.exists(att, t3)

        when: "update - keep t2,t3, remove t1"
        TagLink.addOrRemoveTags(att, [op:"update", data:[[op:"remove", id:t1.id]]])
        flush()

        then:
        TagLink.count() == 2
        !TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        TagLink.exists(att, t3)
        !att.tags.contains(t1)

        when: "no change - same as existing"
        TagLink.addOrRemoveTags(att, [op:"update", data:[[id:t1.id],[id:t2.id],[id:t3.id]]])
        flush()

        then: "should keep em all"
        TagLink.count() == 3
        TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        TagLink.exists(att, t3)

        when: "update - empty"
        TagLink.addOrRemoveTags(att, [op:"update", data:[]])
        flush()

        then: "Should do nothing"
        TagLink.count() == 3
        TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        TagLink.exists(att, t3)
    }

    void "test add remove - loose json string "() {
        setup:
        Attachment att = new Attachment(name: 'foo', location: 'foo').persist()
        Tag t1 = Tag.create(name: 't1', code:'t1', entityName: 'Attachment')
        Tag t2 = Tag.create(name: 't2', code:'t2', entityName: 'Attachment')
        Tag t3 = Tag.create(name: 't3', code:'t3', entityName: 'Attachment')
        flush()

        when: "add 1,2"
        String jstring = "[{id:${t1.id}}, {id:${t2.id}}]"
        TagLink.addOrRemoveTags(att, jstring)
        flush()

        then:
        TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        !TagLink.exists(att, t3)

        when: "add 2,3 remove 1"
        String jstring2 = "[{id:${t2.id}}, {id:${t3.id}}]"
        TagLink.addOrRemoveTags(att, jstring2)
        flush()

        then:
        TagLink.count() == 2
        !TagLink.exists(att, t1)
        TagLink.exists(att, t2)
        TagLink.exists(att, t3)
        !att.tags.contains(t1)
    }

}
