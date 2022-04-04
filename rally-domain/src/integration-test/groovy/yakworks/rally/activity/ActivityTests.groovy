package yakworks.rally.activity

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.RandomStringUtils

import grails.gorm.transactions.Rollback
import grails.plugin.viewtools.AppResourceLoader
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.tag.model.Tag

import static yakworks.rally.activity.model.Activity.Kind as ActKind
import static yakworks.rally.activity.model.Activity.VisibleTo

@Integration
@Rollback
class ActivityTests extends Specification implements DomainIntTest {
    ActivityRepo activityRepo
    AppResourceLoader appResourceLoader
    AttachmentRepo attachmentRepo

    static Map getNoteParams() {
        return [org: [id: 205], note: [body: 'Todays test note']]
    }

    List makeTags(){
        Tag tag1 = new Tag(id:9, code: "Tag1", entityName: "Activity").persist(flush:true)
        Tag tag2 = new Tag(id:10, code: "Tag2", entityName: "Activity").persist(flush:true)
        return [tag1, tag2]
    }

    void "create note"() {
        setup:
        Org org = Org.first()
        Map params = [org:[id: org.id], name: "test-note", attachments: [], task: [state: 0]]

        when:
        Activity result = activityRepo.create(params)
        flush()

        then:
        result != null
        result.name == "test-note"
        result.kind == ActKind.Note

        when: "update"
        result = activityRepo.update([id: result.id, name: "test-updated", kind: "Note", visibleTo: 'Owner'])

        then:
        result != null
        result.name == "test-updated"
        result.visibleTo == VisibleTo.Owner
    }

    void "update note"() {
        when:
        Map params = [id: 22, note: [body: 'placeholder']]
        params.note.body = RandomStringUtils.randomAlphabetic(300)
        Activity result = activityRepo.update(params)
        flushAndClear()
        Activity updatedActivity = Activity.get(params.id)

        then:
        updatedActivity.note != null
        updatedActivity.name.length() == 255
        updatedActivity.note.body.length() == 300
        params.note.body == updatedActivity.note.body
    }

    @Ignore //FIXME need to fix attachments delteing, should be tested and working in gorm-tools first
    def "delete attachments in update"() {
        when:
        Map params = [id: 22, note: [body: 'Test updated Note body']]
        Activity activity = Activity.get(22)
        File startFile = new File(appResourceLoader.rootLocation, "freemarker/grails_logo.jpg")
        byte[] bytes = FileUtils.readFileToByteArray(startFile)
        File tmpFile = appResourceLoader.createTempFile('grails_logo.jpg', bytes)
        String tempFileName = appResourceLoader.getRelativeTempPath(tmpFile)
        Attachment attachment = attachmentRepo.create([name: tempFileName, bytes: bytes])
        String attachmentLocation = attachment.location
        activity.addAttachment(attachment)
        activity.persist()
        flush()

        then:
        activity.attachments.size() == 1

        when: "we flag it for delete"
        flushAndClear()
        params.deleteAttachments = [attachment.id]
        Map updateParm = [
            id: 22, attachments: [ [op:'remove', id: attachment.id] ]
        ]

        Activity updateAct = activityRepo.update(updateParm)
        flush()

        then:
        updateAct
        updateAct.attachments.size() == 0
        !appResourceLoader.getFile(attachmentLocation).exists()

        when:
        flushAndClear()
        Activity updatedActivity = Activity.get(params.id)

        then:
        params.note.body == updatedActivity.note.body
        updatedActivity.attachments.size() == 0

        when:
        //Checking saved ActivityAttachment
        def links = AttachmentLink.list(updatedActivity)

        then:
        !links

    }

    void "add tags"() {
        setup:
        Org org = Org.first()
        def (tag1, tag2) = makeTags()

        when:
        def params = [org:org, kind:"Note", name: RandomStringUtils.randomAlphabetic(100)]
        params.tags = [[id:tag1.id], [id:tag2.id]]


        Activity activity = activityRepo.create(params)
        assert activity != null
        flush()

        List tags = activity.tags

        then:
        tags
        tags.size() == 2
        tags.contains(tag1)
        tags.contains(tag2)
    }

    void addTagsForSearch(){
        makeTags()
        Org org = Org.first()
        def params = [org:org, kind:"Note", name: RandomStringUtils.randomAlphabetic(100)]
        params.tags = [[id:9]]
        activityRepo.create(params)
        activityRepo.create(params)
        params.tags = [[id:9], [id:10]]
        activityRepo.create(params)
        params.tags = [[id:10]]
        activityRepo.create(params)
        flushAndClear()
    }

    void "add tags and make sure filter works"() {
        when:
        addTagsForSearch()

        def actCrit = { tagList ->
            return Activity.query(tags: tagList)
        }
        def crit = Activity.query(tags: [[id:9]] )
        List hasTag1 = crit.list()

        List hasTag2 = actCrit([ [id:10] ]).list()
        List has1or2 = actCrit([ [id:9] , [id:10] ]).list()

        then:
        hasTag1.size() == 3
        hasTag2.size() == 2
        has1or2.size() == 4
    }

}
