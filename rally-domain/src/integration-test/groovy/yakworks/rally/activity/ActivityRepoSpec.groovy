package yakworks.rally.activity

import org.apache.commons.io.IOUtils

import java.time.LocalDateTime

import gorm.tools.repository.RepoUtil
import gorm.tools.security.domain.AppUser
import gorm.tools.security.services.SecService
import grails.plugin.viewtools.AppResourceLoader
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.jdbc.core.JdbcTemplate

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
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.tag.model.Tag
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.tag.model.TagLink

import static yakworks.rally.activity.model.Activity.Kind as ActKind
import static yakworks.rally.activity.model.Activity.VisibleTo

@Integration
@Rollback
class ActivityRepoSpec extends Specification implements DomainIntTest {
    ActivityRepo activityRepo
    AppResourceLoader appResourceLoader
    JdbcTemplate jdbcTemplate
    SecService secService
    AttachmentRepo attachmentRepo

    void "test foo"() {
        when:
        Activity activity = Activity.get(200)

        then:
        200 == activity.id
        0 == activity.attachments.size()

    }

    void "doAssociations attachments"() {
        when:
        def params = [:]
        params['attachments'] = [getTestAttachment()]
        Activity activity = Activity.get(200)

        then:
        200 == activity.id
        0 == activity.attachments.size()

        when:
        activityRepo.doAssociations(activity, params)
        flush()
        Attachment attachment = activity.attachments[0]

        then:
        1 == activity.attachments.size()

        attachment != null
        attachment.id != null

        activity.attachments.each {
            assert 'jpg' == it.extension
            assert 'grails_logo.jpg' == it.name
            assert it.location.endsWith('.jpg')
        }

        200 == activity.org.id

    }

    void "doAssociations Attachment In Params"() {
        when:

        def activity = Activity.get(200)
        appResourceLoader.rootLocation
        File file = new File(appResourceLoader.rootLocation, "freemarker/grails_logo.jpg")
        String fileName = FilenameUtils.getBaseName(file.name)
        String extension = FilenameUtils.getExtension(file.name)
        byte[] data = FileUtils.readFileToByteArray(file)

        Map params = [
            attachments: [
                [ originalFileName: fileName, name: fileName, extension: extension, bytes: data ]
            ]
        ]

        activityRepo.doAssociations(activity, params)
        flush()

        then:
        1 == activity.attachments.size()
        activity.attachments.each {
            'jpg' == it.extension
            'grails_logo' == it.name
            it.location.endsWith('.jpg')
        }
    }

    /*
     * Testcase for saving note with attachment
     */

    void testSaveNote_WithAttachments() {
        given:
        Map params = getNoteParams()

        and:
        params['attachments'] = [getTestAttachment()]

        when:
        Activity activity = activityRepo.create(params)
        flush()

        then:
        activity != null
        getNoteParams().title == activity.title
        Activity.Kind.Note == activity.kind

        when:
        Attachment attachment = activity.attachments[0]

        then:
        noExceptionThrown()
        activity != null
        attachment != null
        attachment.id != null
        'grails_logo.jpg' == attachment.name
        activity.task == null

        cleanup:
        FileUtils.deleteDirectory(appResourceLoader.getLocation("attachments.location"))
    }

    /*
     * Testcase for saving note with both attachment and task
     */

    void testSaveNote_WithAttachmentAndTask() {
        when:
        Map params = getNoteParams()
        params.attachments = [getTestAttachment()]
        params.task = [dueDate: "2016-09-09"]
        params.task.userId = 50
        Activity activity = activityRepo.create(params)
        flushAndClear()

        then:
        activity != null
        Activity.Kind.Todo == activity.kind
        //Checking saved task related to activity
        when:
        Task task = activity.task

        then:
        task != null
        Activity.Kind.Todo == task.taskType.kind
        //Checking saved ActivityAttachment

        when:
        Attachment attachment = activity.attachments[0]

        then:
        attachment != null
        attachment.id != null
        'grails_logo.jpg' == attachment.name

        cleanup:
        //clean up the attachment files
        FileUtils.deleteDirectory(appResourceLoader.getLocation("attachments.location"))

    }

    void createSimpleNote() {
        setup:
        Org org = Org.first()
        Map params = [org:[id: org.id], summary: "test-note", attachments: [], task: [state: 0]]

        when:
        Activity result = activityRepo.create(params)
        flush()

        then:
        result != null
        result.summary == "test-note"
        result.kind == ActKind.Note

        when: "update"
        result = activityRepo.update([id: result.id, summary: "test-updated", kind: "Note", visibleTo: 'Owner'])

        then:
        result != null
        result.summary == "test-updated"
        result.visibleTo == VisibleTo.Owner
    }

    /*
     * Testcase for updating existing note
     */

    void testUpdateSimpleNote() {
        when:
        Map params = [id: 22, note: [body: 'placeholder']]
        params.note.body = RandomStringUtils.randomAlphabetic(300)
        Activity result = activityRepo.update(params)
        flushAndClear()
        Activity updatedActivity = Activity.get(params.id)

        then:
        updatedActivity.note != null
        updatedActivity.summary.length() == 255
        updatedActivity.note.body.length() == 300
        params.note.body == updatedActivity.note.body
    }

    /*
     * Testcase for updating note with attachment
     */

    void testUpdateNote_WithAttachments() {
        when:
        Map params = [id: 22, note: [body: 'Test updated Note body']]
        params['attachments'] = [getTestAttachment()]

        Activity result = activityRepo.update(params)
        flushAndClear()

        then:
        result
        result.note
        result.note.body == params.note.body

        when:
        Activity updatedActivity = Activity.get(params.id)

        then:
        params.note.body == updatedActivity.note.body

        when:
        def attachment = updatedActivity.attachments[0]

        then:
        attachment != null
        attachment.id != null
        'grails_logo.jpg' == attachment.name

        cleanup:
        FileUtils.deleteDirectory(appResourceLoader.getLocation("attachments.location"))
    }

    /*
     * Testcase for GB-986
     */
    void testGetActivityByArTran_ForCompany() {

        when:
        //Setting visibleTo as Company
        jdbcTemplate.update("update Activity set VisibleTo = 'Company' where id = 202")
        jdbcTemplate.update("update ActivityLink set linkedEntity = 'ArTran'")
        Map params = [page: 1, max: 1, allowedMax: 100, recordCount: 0]
        List<Activity> result = activityRepo.listByLinked(459, 'ArTran', params)

        then:
        result.size() >= 1

        when:
        Activity activity = result[0]

        then:
        202 == activity.id
        VisibleTo.Company == activity.visibleTo
        activity.links.each { activityLink ->
            459 == activityLink.linkedId
        }
    }

    void testGetActivityByArTran_ActivityLink_restiction() {

        when:
        //Setting visibleTo as Company
        jdbcTemplate.update("update Activity set VisibleTo = 'Everyone' where id = 202")
        jdbcTemplate.update("update ActivityLink set linkedEntity = 'ArTran'")
        def pagerParams = [page: 1, max: 1, allowedMax: 100, recordCount: 0]
        def result = activityRepo.listByLinked(459, 'ArTran', pagerParams)
        then:
        result.size() == 1

        when:

        jdbcTemplate.update("update ActivityLink set linkedEntity = 'Payment'")
        flushAndClear()
        List result2 = activityRepo.listByLinked(459, 'ArTran', pagerParams)

        then:
        result2.size() == 0
    }

    void testGetActivityByArTran_NotFoundForCustomer() {

        when:
        //Setting visibleTo as Company
        jdbcTemplate.update("update Activity set VisibleTo = 'Company' where id in (202,203,204,205)")
        jdbcTemplate.update("update ActivityLink set linkedEntity = 'ArTran'")
        Map params = [custArea: true, page: 1, max: 1, allowedMax: 100, recordCount: 0]
        List<Activity> result = activityRepo.listByLinked(459, 'ArTran', params)

        then:
        //If custArea is true and visibleTo is set to company, then that activity won't be available to customer
        0 == result.size()
    }

    void testGetActivityByArTran_FoundForCustomer() {

        when:
        //Setting visibleTo as Everyone
        jdbcTemplate.update("update Activity set VisibleTo = 'Everyone' where id = 202")
        jdbcTemplate.update("update ActivityLink set linkedEntity = 'ArTran'")
        Map params = [custArea: true, page: 1, max: 1, allowedMax: 100, recordCount: 0]
        List<Activity> result = activityRepo.listByLinked(459, 'ArTran', params)

        then:
        result.size() >= 1

        when:
        Activity activity = result[0]

        then:
        202 == activity.id
        VisibleTo.Everyone == activity.visibleTo
        activity.links.each { activityLink ->
            459 == activityLink.linkedId
        }
    }

    void testGetActivityByArTran_NotFoundForOwner() {
        when:
        //Setting visibleTo as Owner
        jdbcTemplate.update("update Activity set VisibleTo = 'Owner' where id in (202,203,204,205)")
        jdbcTemplate.update("update ActivityLink set linkedEntity = 'ArTran'")
        Map params = [page: 1, max: 1, allowedMax: 100, recordCount: 0]
        List<Activity> result = activityRepo.listByLinked(459, 'ArTran', params)

        then:
        //If visibleTo is set to owner and createdBy is different then logged in user, then that activity won't be available to customer
        0 == result.size()
    }

    void testGetActivityByArTran_FoundForOwner() {
        setup:
        secService.loginAsSystemUser()

        when:
        //Setting visibleTo as Owner and CreatedBy same as logged in user
        //        jdbcTemplate.update("update Activity set VisibleTo = 'Owner', CreatedBy = 50 where id = 202")
        jdbcTemplate.update("update ActivityLink set linkedEntity = 'ArTran'")
        Activity.get(202).with {
            visibleTo = VisibleTo.Owner
            createdBy = secService.userId
            assert links.any { it.linkedId == 459 }
            persist(flush: true)
        }
        RepoUtil.flushAndClear()
        Map params = [page: 1, max: 10, allowedMax: 100, recordCount: 0]
        List<Activity> result = activityRepo.listByLinked(459, 'ArTran', params)

        then:
        result.size() >= 1
        result.any { it.id == 202 }

        when:
        Activity activity = result.find { it.id == 202 }

        then:
        VisibleTo.Owner == activity.visibleTo
        activity.links.each { activityLink ->
            459 == activityLink.linkedId
        }
    }

    static Map getNoteParams() {
        return [org: [id: 205], title: 'Todays test note', note: [body: 'Todays test note'], summary: '2+3=5']
    }

    Map getTestAttachment() {
        File startFile = new File(appResourceLoader.rootLocation, "freemarker/grails_logo.jpg")
        byte[] bytes = FileUtils.readFileToByteArray(startFile)
        File tmpFile = appResourceLoader.createTempFile('grails_logo.jpg', bytes)
        String tempFileName = appResourceLoader.getRelativeTempPath(tmpFile)

        return [tempFileName: tempFileName, originalFileName: 'grails_logo.jpg']
    }

    @Ignore //XXX need to fix activity copy https://github.com/9ci/domain9/issues/271
    void testCopy() {
        when:
        Org org = Org.first()
        Org last = Org.last()
        Activity activity = new Activity(org: org, title: "title", summary: "summary")
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
        copy.title == activity.title
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

    @Ignore //XXX need to fix attachments delteing, should be tested and working in gorm-tools first
    def testUpdateWithDeleteList() {
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

    def "test insert activity with task"() {
        given:
        Org org = Org.first()
        Map params = [summary: "yyy", kind: "Todo", title: 'yyy', org: [id: org.id], task: [dueDate: '2017-06-22', taskType: [id: TaskType.EMAIL.id], state: 0, priority: 10]]

        when:
        Activity activity = activityRepo.create(params)
        flush()

        then:
        activity.id != null
        activity.title == "yyy"
        activity.summary == "yyy"
        activity.task != null
        activity.task.taskType == TaskType.EMAIL
        activity.task.state == Task.State.Open
        activity.task.priority == 10
        activity.task.id != null
        activity.note == null
    }

    /*
     * Testcase for saving note with task
     */

    void testSaveNote_WithTask() {
        when:
        Map params = getNoteParams()
        params.task = [dueDate: "2016-09-09", userId: 50]
        Activity activity = activityRepo.create(params)
        flushAndClear()

        then:
        activity != null
        params.title == activity.title
        Activity.Kind.Todo == activity.kind

        when:
        Task task = activity.task

        then:
        task != null
        Activity.Kind.Todo == task.taskType.kind
        params.task.userId == task.userId
    }

    void testInsertTODOTask() {
        when:
        Org org = Org.last()
        AppUser user = AppUser.last()
        Activity activity = activityRepo.createTodo(org, user.id, "test Title")
        then:
        activity != null
        activity.org == org
        activity.title == "test Title"
        activity.kind == Activity.Kind.Todo
        activity.task != null
        activity.task.taskType == TaskType.TODO
        activity.task.userId == user.id
        activity.task.dueDate != null
        activity.task.status == TaskStatus.getOPEN()
    }

    void updateSimpleNote() {
        setup:
        Org org = Org.first()
        Map params = [org:[id: org.id], summary: "test-note", attachments: [], task: [state: 0]]
        Activity result = activityRepo.create(params)
        flush()

        when:
        Map newParams = [id: result.id, summary: "new test-note", attachments: [], task: [state: 0]]
        activityRepo.update(newParams)

        then:
        result != null
        result.summary == "new test-note"
        result.kind == ActKind.Note
    }

    void testInsertMassNote() {
        setup:
        Org org = Org.create("T01", "T01", OrgType.Customer)
        List<Org> customers = Org.findAllByOrgTypeId(OrgType.Customer.id, [max:5])
        assert customers.size() == 5

        when:
        Activity activity = activityRepo.insertMassNote(customers, "Customer", org, "test note", "test note")
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

    void testSetTags() {
        setup:
        Org org = Org.first()
        Tag tag1 = new Tag(code: "Tag1", entityName: "Activity").persist()
        Tag tag2 = new Tag(code: "Tag2", entityName: "Activity").persist()
        flush()

        when:
        def params = [org:org, kind:"Note", summary: RandomStringUtils.randomAlphabetic(100)]
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

}
