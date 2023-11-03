package yakworks.rally.attachment

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class AttachmentRepoSpec extends Specification implements DomainIntTest {

    AttachmentRepo attachmentRepo
    AttachmentSupport attachmentSupport

    def testInsert_works() {
        when:
        Map params = [
            name:'hello.txt', subject:'greetings', bytes: 'blah blah blah'.getBytes()
        ]
        Attachment attachment = attachmentRepo.create(params)

        then:
        attachment
        attachment instanceof Attachment
        attachment.id
        attachment.name
        attachment.version != null
        "text/plain" == attachment.mimeType
        14 == attachment.contentLength
        'txt' == attachment.extension

        cleanup:
        attachment.resource.file?.delete()
    }

    def testRemove_works() {
        when:
        Attachment attachment = attachmentRepo.create([name: 'hello.txt', bytes: 'blah blah blah'.getBytes()])
        def id = attachment.id
        attachmentRepo.flushAndClear()
        attachmentRepo.remove(attachment)

        then:
        null == Attachment.get(id)
    }

    def testUpdate_works() {
        when:
        Map params = [name:'hello.txt', subject:'greetings', bytes: 'blah blah blah'.getBytes()]
        Attachment attachment = attachmentRepo.create(params)
        flushAndClear()

        final String newName = 'Something Completely Different'
        Attachment att = Attachment.get(attachment.id)

        then:
        att != null

        when:
        att.name = newName
        att.persist(flush:true)
        Attachment a2 = Attachment.get(attachment.id)

        then:
        newName == a2.name
    }

    def testCopyCreatesNewAttachment() {
        when:
        Attachment old = attachmentRepo.create([name: 'hello.txt', bytes: 'blah blah blah'.getBytes()])
        old.description = 'test desc'
        old.persist(flush: true)

        then:
        old != null

        when:
        Attachment copy = attachmentRepo.copy(old)

        then:
        copy != null
        !copy.is(old)
        copy.id != null
        old.name == copy.name
        old.description == copy.description
        copy.location != null
        copy.location != old.location
        copy.extension != null

        when:
        File file = copy.resource.file
        println copy.id
        println file.absolutePath

        then:
        copy.extension ==  old.extension
        file != null
        file.exists()
        file.delete()
        //verfiy it doesnt change old attachment
        old.inputStream != null
    }


}
