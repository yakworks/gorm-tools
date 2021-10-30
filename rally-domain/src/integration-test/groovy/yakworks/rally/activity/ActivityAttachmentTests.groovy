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
class ActivityAttachmentTests extends Specification implements DomainIntTest {
    ActivityRepo activityRepo
    AppResourceLoader appResourceLoader
    JdbcTemplate jdbcTemplate
    SecService secService
    AttachmentRepo attachmentRepo

    static Map getNoteParams() {
        return [org: [id: 205], note: [body: 'Todays test note'], summary: '2+3=5']
    }

    Map getTestAttachment(String filename) {
        File tmpFile = appResourceLoader.createTempFile(filename, "test text".getBytes())
        String tempFileName = appResourceLoader.getRelativeTempPath(tmpFile)

        return [tempFileName: tempFileName, originalFileName: filename]
    }

    void "doAssociations attachments"() {
        when:
        def params = [:]
        params['attachments'] = [getTestAttachment('testing.txt')]
        Activity activity = Activity.get(9)
        assert !activity.attachments

        activityRepo.doAssociations(activity, params)
        flush()
        Attachment attachment = activity.attachments[0]

        then:
        1 == activity.attachments.size()

        attachment != null
        attachment.id != null

        activity.attachments.each {
            assert 'txt' == it.extension
            assert 'testing.txt' == it.name
            assert it.location.endsWith('.txt')
        }

        200 == activity.org.id

    }

    void "doAssociations Attachment In Params"() {
        when:
        def activity = Activity.get(9)
        Map params = [
            attachments: [
                [ originalFileName: 'foo.jpg', extension: 'jpg', bytes: 'foo'.getBytes() ]
            ]
        ]

        activityRepo.doAssociations(activity, params)
        flush()

        then:
        1 == activity.attachments.size()
        activity.attachments.each {
            'jpg' == it.extension
            'foo.jpg' == it.name
            it.location.endsWith('.jpg')
        }
    }

    void "not with attachments"() {
        given:
        Map params = getNoteParams()

        and:
        params['attachments'] = [getTestAttachment('grails_logo.jpg')]

        when:
        Activity activity = activityRepo.create(params)
        flush()

        then:
        activity != null
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

    void "update note with attachments"() {
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


    void testHasAttachments(){
        when:
        def activity = Activity.get(200)//Existing Activity in test d/b without ActivityAttachment

        then:
        activity != null
        !activity.attachments
    }

    void testHasAttachments_Success(){
        when:
        //Adding attachment to existing Activity from test d/b to check for hasAttachments
        def activity = Activity.get(200)
        def att = Attachment.get(1004)
        assert att
        activity.addAttachment(att)
        activity.persist()
        flush()
        then:
        activity.hasAttachments()
    }

}
