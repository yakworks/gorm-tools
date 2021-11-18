package yakworks.rally.attachment


import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

import grails.gorm.transactions.Rollback
import grails.plugin.viewtools.AppResourceLoader
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentRepo

@Integration
@Rollback
class AttachmentRepoSpec extends Specification implements DomainIntTest {

    static final String RESOURCES_PATH = "src/integration-test/resources"

    AppResourceLoader appResourceLoader
    AttachmentRepo attachmentRepo
    AttachmentSupport attachmentSupport

    // void cleanupSpec() {
    //     FileUtils.deleteDirectory(appResourceLoader.getLocation("attachments.location"))
    // }

    @Ignore //FIXME https://github.com/9ci/domain9/issues/331
    def "bulkCreate test"() {
        setup:
        File origFile = new File(appResourceLoader.rootLocation, "freemarker/grails_logo.jpg")
        byte[] data = FileUtils.readFileToByteArray(origFile)
        File tmpFile = appResourceLoader.createTempFile('grails_logo.jpg', data)
        tmpFile.deleteOnExit()
        File tmpFile2 = appResourceLoader.createTempFile('grails_logo2.jpg', data)
        tmpFile2.deleteOnExit()

        List list = []
        list.add([tempFileName:"${tmpFile.name}",originalFileName:'grails_logo.jpg'])

        list.add([tempFileName:"${tmpFile2.name}",originalFileName:'grails_logo2.jpg'])

        when:
        List attachments = attachmentRepo.bulkCreate(list)

        then:
        2 == attachments.size()

        when:
        Attachment attachment = attachments[0]

        then:
        attachment.fileData == null
        attachment.location != null

        when:
        File attachedFile = appResourceLoader.getFile(attachment.location)

        then:
        attachedFile.exists()
        origFile.size() == attachment.contentLength

        origFile.size() == attachedFile.size()
        'grails_logo.jpg' == attachment.name
        'jpg'== attachment.extension
        "image/jpeg" == attachment.mimeType
        'jpg' == FilenameUtils.getExtension(attachedFile.name)

        cleanup:
        attachedFile.delete()
        tmpFile.delete()

    }

    def testInsert_works() {
        when:
        Map params = [name:'hello.txt', subject:'greetings']
        params.bytes = 'blah blah blah'.getBytes()
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
        File attachedFile = appResourceLoader.getFile(attachment.location)
        attachedFile?.delete()
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
        File file = appResourceLoader.getFile(copy.location)
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
