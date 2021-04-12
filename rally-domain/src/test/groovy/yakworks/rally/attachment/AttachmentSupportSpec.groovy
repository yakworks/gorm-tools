package yakworks.rally.attachment

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.springframework.mock.web.MockMultipartFile

import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.FileData

class AttachmentSupportSpec extends Specification implements DomainRepoTest<Attachment>, SecurityTest {

    @Shared
    AttachmentSupport attachmentSupport

    def setupSpec() {
        defineBeans({
            attachmentSupport(AttachmentSupport)
        })
        //mockDomains(Attachment)
    }

    // void cleanupSpec() {
    //     FileUtils.deleteDirectory(appResourceLoader.getLocation("attachments.location"))
    // }

    // gets a file from example/resources
    Path getFile(String name){
        Paths.get(System.getProperty("gradle.rootProjectDir"), "examples/resources/$name")
    }

    @IgnoreRest
    def "test createFileFromSource"() {
        when:
        def fileName = 'test.txt'
        Path sourcePath = getFile(fileName)
        Path createdFile = attachmentSupport.createFileFromSource(123, fileName, sourcePath)

        then:
        Files.exists(createdFile)
        createdFile.toString() == 'foo'
    }


}
