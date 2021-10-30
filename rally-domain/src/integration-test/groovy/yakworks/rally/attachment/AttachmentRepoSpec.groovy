package yakworks.rally.attachment

import gorm.tools.repository.errors.EntityValidationException
import grails.gorm.transactions.Rollback
import grails.plugin.viewtools.AppResourceLoader
import grails.testing.mixin.integration.Integration

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.springframework.mock.web.MockMultipartFile

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import yakworks.gorm.testing.DomainIntTest
import yakworks.gorm.testing.SecuritySpecHelper
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentRepo

@Integration
@Rollback
class AttachmentRepoSpec extends Specification implements DomainIntTest {

    static final String RESOURCES_PATH = "src/integration-test/resources"

    AppResourceLoader appResourceLoader
    AttachmentRepo attachmentRepo

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

    def testDeleteFileIfInsert_fail() {
        when:
        File origFile = new File(appResourceLoader.rootLocation, "freemarker/grails_logo.jpg")
        byte[] data = FileUtils.readFileToByteArray(origFile)
        File tmpFile = appResourceLoader.createTempFile('grails_logo.jpg', data)
        assert tmpFile.exists()
        Map params = [tempFileName: tmpFile.name, id:12345999999L]
        Attachment result = attachmentRepo.create(params)

        then:
        EntityValidationException g = thrown()
        'validation.error' == g.code
        String destFileName = tmpFile.name.split("/")[-1]+"_12345999999.jpg"
        File monthDir = appResourceLoader.getMonthDirectory("attachments.location")
        File testFile = new File(monthDir.path, destFileName)
        assert !testFile.exists()
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


    def testInsertMultipartFile_works() {
        when:
        File inputFile = new File(appResourceLoader.rootLocation, "freemarker/grails_logo.jpg")
        byte[] bytes = FileUtils.readFileToByteArray(inputFile)
        MockMultipartFile file = new MockMultipartFile("file", "grails_logo.jpg", "multipart/form-data", bytes);
        Attachment entity = attachmentRepo.create(file, [:]);
        File attachedFile = appResourceLoader.getFile(entity.location)

        then:
        entity
        'jpg' == entity.extension
        "image/jpeg" == entity.mimeType

        entity.location == appResourceLoader.getRelativePath('attachments.location', attachedFile)

        cleanup:
        attachmentRepo.remove(entity)
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

    def testUpdate_fail() {
        when:
        Attachment attachment = Attachment.get(1005)

        then:
        attachment != null

        when:
        attachment.name = null;
        attachment.persist(flush:true)

        then:
        EntityValidationException e = thrown()
        e.message.contains('rejected value [null]')

    }

    def testUpdate_works() {
        when:
        final String newName = 'Something Completely Different'
        Attachment attachment = Attachment.get(1005)

        then:
        attachment != null

        when:
        attachment.name = newName
        attachment.persist(flush:true)
        Attachment a2 = Attachment.get(1005)

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
