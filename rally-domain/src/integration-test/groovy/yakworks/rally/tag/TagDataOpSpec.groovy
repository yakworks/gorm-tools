package yakworks.rally.tag


import yakworks.testing.gorm.integration.SecuritySpecHelper
import yakworks.testing.gorm.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink

/**
 * test the data ops that come in from rest
 * using attachment with tags to test adding and removing links
 */
@Integration
@Rollback
class TagDataOpSpec extends Specification implements DataIntegrationTest, SecuritySpecHelper {

    List createSomeTags(){
        List tags = []
        (0..9).each { eid ->
            tags <<  Tag.create(name: "tag$eid", code: "tag$eid")
        }
        // only valid for attachment
        tags <<  Tag.create(name: 'onlyAttachment', code: 'onlyAttach', entityName: 'Attachment')
        // valid for 2
        tags <<  Tag.create(name: 'cross', code: 'cross', entityName: 'Contact, Attachment')
        // valid only for Contact
        tags <<  Tag.create(name: 'manager', code: 'manager', entityName: 'Contact')
        flushAndClear()
        return tags
    }

    Attachment setupAnAttachmentWithTags(List tags = null){
        if(!tags) tags = createSomeTags()

        flush()
        def attachData = [
            name: 'foo.txt',
            tags: [
                [id: tags[0].id],
                [id: tags[1].id],
                [id: tags[2].id]
            ]
        ]
        def a = Attachment.create(attachData)
        flush()
        assert a.tags.size() == 3
        return a
    }

    void "sanity check"() {
        when:
        def att = setupAnAttachmentWithTags()
        flush() //needed for the checks as it queries

        then:
        att.hasTags()
        att.getTags().size() == 3
        TagLink.list(att).size() == 3
    }

    void "replace is the default if nothing specified"() {
        when:
        def tags = createSomeTags()
        def att = setupAnAttachmentWithTags(tags)

        def id = att.id
        def dta = [
            id: att.id,
            tags: [
                [id: tags[3].id],
                [id: tags[4].id]
            ]
        ]
        def updatedAtt = Attachment.update(dta)
        flush()
        then:
        updatedAtt.tags.size() == 2

    }

    void "if a tag already exists then will simple keep it and not add it"() {
        when:
        def tags = createSomeTags()
        def att = setupAnAttachmentWithTags(tags)

        def id = att.id
        def dta = [
            id: att.id,
            tags: [
                [id: tags[0].id], //already exists
                [id: tags[3].id],
                [id: tags[4].id]
            ]
        ]
        def updatedAtt = Attachment.update(dta)
        flush()
        then:
        updatedAtt.tags.size() == 3

    }

    void "test op:update with empty array does nothing"() {
        when:
        def att = setupAnAttachmentWithTags()
        flushAndClear()

        def id = att.id
        def dta = [
            id: att.id,
            tags: [
                op:'update', data: []
            ]
        ]
        def updatedAtt = Attachment.update(dta)

        then: "still has them"
        updatedAtt.hasTags()
        TagLink.list(updatedAtt).size() == 3
    }

    void "test op:replace deafult with empty list"() {
        when:
        def att = setupAnAttachmentWithTags()
        flushAndClear()

        def id = att.id
        def dta = [
            id: att.id,
            tags: []
        ]
        def updatedAtt = Attachment.update(dta)

        then: "still has them"
        !updatedAtt.hasTags()
        TagLink.list(updatedAtt).size() == 0
    }

    void "test op:remove on one"() {
        when:
        def att = setupAnAttachmentWithTags()
        def tag1Id = att.tags[0].id
        assert tag1Id
        flushAndClear()

        def id = att.id
        def dta = [
            id: att.id,
            tags: [
                op:'update', data: [
                    [ op:'remove', id: tag1Id ]
                ]
            ]
        ]
        def updatedAtt = Attachment.update(dta)

        then:
        updatedAtt.tags.size() == 2

    }

    void "test op:update replace"() {
        when:
        def tags = createSomeTags()
        def att = setupAnAttachmentWithTags(tags)

        def id = att.id
        def dta = [
            id: att.id,
            tags: [
                op:'update',
                data: [
                    [id: tags[0].id], //this was already here and should remain
                    [id: tags[1].id], //this was already here and should remain
                    [id: tags[3].id],
                    [id: tags[4].id],
                ]
                //so the above data only adds 2, there is 1 that exists not mentioned and it will remain
            ]
        ]
        def updatedAtt = Attachment.update(dta)
        flush()
        then: "it kept the 2 existing and added 2 to the 3 that existed"
        updatedAtt.tags.size() == 5
        updatedAtt.tags[0].name == 'tag0'
        updatedAtt.tags[1].name == 'tag1'
        updatedAtt.tags[2].name == 'tag2' //still here even though not refed above
        updatedAtt.tags[3].name == 'tag3'
        updatedAtt.tags[4].name == 'tag4'
    }

}
