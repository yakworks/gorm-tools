package yakworks.rally.activity

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.apache.commons.io.IOUtils
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.DomainIntTest
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink

@Integration
@Rollback
class ActivityCopyTests extends Specification implements DomainIntTest {
    ActivityRepo activityRepo
    AttachmentSupport attachmentSupport

    @Ignore //FIXME need to fix activity copy https://github.com/9ci/domain9/issues/271
    void testCopy() {
        when:
        Org org = Org.first()
        Org last = Org.last()
        Activity activity = Activity.create([org: org, name: "summary"])
        activity.note = new ActivityNote(body: "body")
        //activity.task = new Task(dueDate: LocalDateTime.now(), status: TaskStatus.OPEN, taskType: TaskType.EMAIL) todo TaskStatus.OPEN does not exist
        activity.persist()

        ActivityLink.repo.create(1000, 'ArTran', activity)

        and:
        Attachment attachment = Attachment.get(1005) //XX this attachments dont exist in gorm-tools test db
        Attachment badAttachment = Attachment.get(1030)

        then:
        attachment != null
        badAttachment != null

        when:
        Contact contact = Contact.first()
        activity.persist(flush: true)

        badAttachment.fileData = null
        badAttachment.location = null
        badAttachment.persist()

        activity.addAttachment(attachment)
        activity.addAttachment(badAttachment)
        //activity.addToContacts(contact)
        flush()
        activity = Activity.get(activity.id)

        then:
        activity
        activity.attachments.size() == 2

        when:
        Tag.update(id: 1, code: 'test', entityName: 'Activity')
        TagLink.addTags(activity, [Tag.load(1)])
        flush()

        then:
        activity.tags.size() == 1

        when:
        Activity copy = activityRepo.copy(activity, new Activity(org: last))
        flush()
        copy = copy.refresh()

        then:
        copy
        copy.id != null
        copy.name == activity.name
        copy.note != null
        copy.note.id != null
        copy.note.id != activity.note.id
        copy.note.body == "body"

        copy.task != null
        copy.task.id != null
        copy.task.id != activity.task.id //should have been created new
        copy.task.status == TaskStatus.OPEN
        copy.task.taskType == TaskType.EMAIL

        //verify attachment
        copy.attachments.size() == 1
        copy.attachments[0].name == attachment.name
        copy.attachments[0].id != attachment.id //should be created new
        copy.attachments[0].contentLength == IOUtils.toByteArray(attachment.inputStream).length

        //verify contact
        copy.contacts.size() == 1
        copy.contacts[0].name.trim() == activity.contacts[0].name.trim() //data in db is bad, remove this in unit test
        //copy.contacts[0].id != activity.contacts[0].id //shoud be created new

        //verify activity link
        copy.links.size() == 1
        copy.links[0].linkedId == 1000L
        copy.links[0].linkedEntity == LinkedEntity.ArTran


        //verify tag
        copy.tags.size() == 1

        cleanup:
        if(copy && copy.attachments) attachmentSupport.getResource(copy.attachments[0]).getFile().delete()
    }

}
