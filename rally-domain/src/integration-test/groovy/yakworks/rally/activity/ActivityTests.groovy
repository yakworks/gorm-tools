package yakworks.rally.activity

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.RandomStringUtils

import grails.gorm.transactions.Rollback
import grails.plugin.viewtools.AppResourceLoader
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.repo.AttachmentRepo
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

    @Ignore //XXX need to fix attachments delteing, should be tested and working in gorm-tools first
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

    void "bulk insert note"() {
        setup:
        Org org = Org.create("T01", "T01", OrgType.Customer)
        List<Org> customers = Org.findAllByOrgTypeId(OrgType.Customer.id, [max:5])
        assert customers.size() == 5

        when:
        Activity activity = activityRepo.insertMassNote(customers, "Customer", org, "test note")
        flush()

        activity = Activity.get(activity.id)
        List<ActivityLink> links = ActivityLink.list(activity)

        then:
        links.size() == 5
        activity.note.body == "test note"
        customers.each { Org customer ->
            assert links.find({ it.linkedId == customer.id}) != null
        }

    }

    void "add tags"() {
        setup:
        Org org = Org.first()
        Tag tag1 = new Tag(code: "Tag1", entityName: "Activity").persist()
        Tag tag2 = new Tag(code: "Tag2", entityName: "Activity").persist()
        flush()

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

    void "save activity with an Org that does not exist"(){

        when:
        def params = [
            org:[id: 909090],
            note:[body: 'Todays test note'],
            name: '!! will get overriden as it has a note !!'
        ]

        Activity act = Activity.create(params)
        flush()

        then:
        act
        act.note.body == params.note.body
        act.name == params.note.body

        when:
        flushAndClear()
        Activity activity = Activity.get(act.id)

        then:
        activity
        activity.note
        activity.orgId == params.org.id.toLong()
        activity.org.id == params.org.id.toLong()
        params.note.body == activity.note.body
        params.note.body == activity.name
        Activity.Kind.Note == activity.kind
        activity.task == null

        when: "Activity is removed, note also gets removed"
        activity.remove()

        then:
        Activity.get(act.id) == null
        ActivityNote.get(activity.noteId) == null
    }

}
