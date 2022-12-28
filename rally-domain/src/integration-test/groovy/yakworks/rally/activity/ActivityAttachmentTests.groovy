package yakworks.rally.activity

import java.nio.file.Path

import gorm.tools.repository.PersistArgs
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore
import spock.lang.Specification
import yakworks.rally.orgs.model.ContactFlex
import yakworks.rally.orgs.model.Org
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment

@Integration
@Rollback
class ActivityAttachmentTests extends Specification implements DomainIntTest {
    ActivityRepo activityRepo
    AttachmentSupport attachmentSupport

    @OnceBefore
    void setupData(){
        Org.list().each { Org org ->
            def act = Activity.create([id: org.id, org: org, note: [body: 'Test note']], bindId: true)
        }
        flushAndClear()
    }

    static Map getNoteParams() {
        return [org: [id: 9], note: [body: 'Todays test note']]
    }

    Map getTestAttachment(String filename) {
        Path tmpFile2 = attachmentSupport.createTempFile(filename, "test text".getBytes())
        String tempFileName = tmpFile2.getFileName().toString()

        return [tempFileName: tempFileName, originalFileName: filename]
    }

    void "doAfterPersistWithData attachments"() {
        when:
        def data = [:]
        data['attachments'] = [getTestAttachment('testing.txt')]
        Activity activity = Activity.get(9)
        assert !activity.attachments

        activityRepo.doAfterPersistWithData(activity, PersistArgs.of(data:data))
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

    void "doAfterPersistWithData Attachment In Params"() {
        when:
        def activity = Activity.get(9)
        Map data = [
            attachments: [
                [ originalFileName: 'foo.jpg', extension: 'jpg', bytes: 'foo'.getBytes() ]
            ]
        ]

        activityRepo.doAfterPersistWithData(activity, PersistArgs.of(data: data))

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
        attachmentSupport.rimrafAttachmentsDirectory()
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
        attachmentSupport.rimrafAttachmentsDirectory()
    }


    void testHasAttachments(){
        when:
        def activity = Activity.get(9)//Existing Activity in test d/b without ActivityAttachment

        then:
        activity
        !activity.attachments
    }

}
