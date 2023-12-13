package yakworks.rally.attachment

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.spring.AppResourceLoader
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class AttachmentBulkSpec extends Specification implements DomainIntTest {

    AppResourceLoader appResourceLoader
    AttachmentRepo attachmentRepo
    AttachmentSupport attachmentSupport

    @Ignore //FIXME need to flush this out
    def "bulkCreate test"() {
        setup:
        File origFilePath = appResourceLoader.rootPath.resolve("freemarker/grails_logo.jpg")
        File origFile = appResourceLoader.rootPath.resolve("freemarker/grails_logo.jpg").toFile()
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
        File attachedFile = attachment.resource.file

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

}
