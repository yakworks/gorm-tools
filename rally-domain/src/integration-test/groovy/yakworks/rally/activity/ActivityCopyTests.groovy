package yakworks.rally.activity

import java.time.LocalDateTime

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.repository.RepoUtil
import gorm.tools.security.services.SecService
import grails.gorm.transactions.Rollback
import grails.plugin.viewtools.AppResourceLoader
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink

import static yakworks.rally.activity.model.Activity.Kind as ActKind
import static yakworks.rally.activity.model.Activity.VisibleTo

@Integration
@Rollback
class ActivityCopyTests extends Specification implements DomainIntTest {
    ActivityRepo activityRepo

    @Ignore //XXX need to fix activity copy https://github.com/9ci/domain9/issues/271
    void testCopy() {
        when:
        Org org = Org.first()
        Org last = Org.last()
        Activity activity = new Activity(org: org, summary: "summary")
        activity.note = new ActivityNote(activity: activity, body: "body")
        activity.task = new Task(activity: activity, dueDate: LocalDateTime.now(), status: TaskStatus.OPEN, taskType: TaskType.EMAIL)
        ActivityLink.repo.create(1000, 'ArTran', activity)

        Attachment attachment = Attachment.get(1005)
        Attachment badAttachment = Attachment.get(1030)

        badAttachment.fileData = null
        badAttachment.location = null
        badAttachment.persist()

        then:
        attachment != null
        badAttachment != null

        when:
        Contact contact = Contact.first()
        activity.persist(flush: true)
        activity.addAttachment(attachment)
        activity.addAttachment(badAttachment)
        activity.addToContacts(contact)
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
        copy.summary == activity.summary
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
        if(copy && copy.attachments) appResourceLoader.getFile(copy.attachments[0].location).delete()
    }

}
