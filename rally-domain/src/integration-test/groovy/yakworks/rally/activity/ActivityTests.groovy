package yakworks.rally.activity

import java.time.LocalDateTime

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
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
class ActivityTests extends Specification implements DomainIntTest {
    ActivityRepo activityRepo
    AppResourceLoader appResourceLoader
    JdbcTemplate jdbcTemplate
    SecService secService
    AttachmentRepo attachmentRepo

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
        return [org: [id: 205], note: [body: 'Todays test note'], summary: '2+3=5']
    }

    Map getTestAttachment(String filename) {
        File tmpFile = appResourceLoader.createTempFile(filename, "test text".getBytes())
        String tempFileName = appResourceLoader.getRelativeTempPath(tmpFile)

        return [tempFileName: tempFileName, originalFileName: filename]
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

    Activity createAct(){
        Map params = [
            org:[id: 99], //org id does not exist
            note:[body: 'Todays test note'],
            summary: 'will get overriden'
        ]
        Activity act = Activity.create(params)
        flush()
        return act
    }

    void "save activity with an Org that does not exist"(){

        when:
        def params = [
            org:[id: 909090],
            note:[body: 'Todays test note'],
            summary: '!! will get overriden as it has a note !!'
        ]

        Activity act = Activity.create(params)
        flush()

        then:
        act
        act.note.body == params.note.body
        act.summary == params.note.body

        when:
        flushAndClear()
        Activity activity = Activity.get(act.id)

        then:
        activity
        activity.note
        activity.orgId == params.org.id.toLong()
        activity.org.id == params.org.id.toLong()
        params.note.body == activity.note.body
        params.note.body == activity.summary
        Activity.Kind.Note == activity.kind
        activity.task == null

        when: "Activity is removed, note also gets removed"
        activity.remove()

        then:
        Activity.get(act.id) == null
        ActivityNote.get(activity.noteId) == null
    }

    def testSaveForContacts() {
        when:
        def activity = new Activity()
    }

}
