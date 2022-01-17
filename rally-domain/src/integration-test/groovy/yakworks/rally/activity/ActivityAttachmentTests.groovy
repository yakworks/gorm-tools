package yakworks.rally.activity

import gorm.tools.repository.PersistArgs

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
    AttachmentRepo attachmentRepo

    static Map getNoteParams() {
        return [org: [id: 9], note: [body: 'Todays test note']]
    }

    Map getTestAttachment(String filename) {
        File tmpFile = appResourceLoader.createTempFile(filename, "test text".getBytes())
        String tempFileName = appResourceLoader.getRelativeTempPath(tmpFile)

        return [tempFileName: tempFileName, originalFileName: filename]
    }

    void "persistToManyData attachments"() {
        when:
        def data = [:]
        data['attachments'] = [getTestAttachment('testing.txt')]
        Activity activity = Activity.get(9)
        assert !activity.attachments

        activityRepo.persistToManyData(activity, PersistArgs.of(data:data))
        flush()
        Attachment attachment = activity.attachments[0]

        then:
        1 == activity.attachments.size()
        attachment.id

        activity.attachments.each {
            assert 'txt' == it.extension
            assert 'testing.txt' == it.name
            assert it.location.endsWith('.txt')
        }

        9 == activity.org.id

    }

    void "persistToManyData Attachment In Params"() {
        when:
        def activity = Activity.get(9)
        Map data = [
            attachments: [
                [ originalFileName: 'foo.jpg', extension: 'jpg', bytes: 'foo'.getBytes() ]
            ]
        ]

        activityRepo.persistToManyData(activity, PersistArgs.of(data: data))

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
        activity.hasAttachments()
        attachment.id
        'grails_logo.jpg' == attachment.name
        !activity.task

        cleanup:
        FileUtils.deleteDirectory(appResourceLoader.getLocation("attachments.location"))
    }

    void "update note with attachments"() {
        when:
        Map params = [id: 22, note: [body: 'Test updated Note body']]
        params['attachments'] = [getTestAttachment('foo.pdf')]

        Activity result = activityRepo.update(params)
        flushAndClear()

        then:
        result
        result.note
        result.note.body == params.note.body

        when:
        Activity updatedActivity = Activity.get(params.id)
        def attachment = updatedActivity.attachments[0]

        then:
        updatedActivity.hasAttachments()
        params.note.body == updatedActivity.note.body

        attachment.id
        'foo.pdf' == attachment.name

        cleanup:
        FileUtils.deleteDirectory(appResourceLoader.getLocation("attachments.location"))
    }


    void testHasAttachments(){
        when:
        def activity = Activity.get(9)//Existing Activity in test d/b without ActivityAttachment

        then:
        activity
        !activity.attachments
    }

}
