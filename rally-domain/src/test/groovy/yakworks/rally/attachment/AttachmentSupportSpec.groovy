package yakworks.rally.attachment

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.commons.util.BuildSupport
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.spring.AppResourceLoader
import yakworks.testing.gorm.SecurityTest
import yakworks.testing.gorm.unit.GormHibernateTest

class AttachmentSupportSpec extends Specification implements GormHibernateTest, SecurityTest {

    @Autowired AttachmentSupport attachmentSupport

    static List<Class> entityClasses = [Attachment, AttachmentLink, Activity, ActivityNote, ActivityLink]

    Closure doWithGormBeans(){ { ->
        appResourceLoader(AppResourceLoader)
        attachmentSupport(AttachmentSupport)
    }}

    def setup() {
        //make sure its clear
        attachmentSupport.rimrafAttachmentsDirectory()
    }

    // gets a file from example/resources
    Path getFile(String name){
        Paths.get(BuildSupport.gradleRootProjectDir, "examples/resources/$name")
    }

    void "test concatFileNameId"() {
        expect:
        'foo_1234.pdf' == AttachmentSupport.concatFileNameId('foo.pdf', 1234)
    }

    void "test createFileFromSource"() {
        when:
        def fileName = 'test.txt'
        Path sourcePath = getFile(fileName)
        Path createdFile = attachmentSupport.createFileFromSource(123, fileName, sourcePath)

        then:
        Files.exists(createdFile)
    }

    void "test createFileFromBytes"() {
        when:
        def fileName = 'createFileFromBytes.txt'
        Path createdFile = attachmentSupport.createFileFromBytes(123, fileName, "foo bar".getBytes())

        then:
        Files.exists(createdFile)
    }

    void "test createFileFromTempFile"() {
        when:
        def fileName = 'grails_logo.jpg'
        byte[] data = Files.readAllBytes(getFile(fileName))
        Path tempFile = attachmentSupport.createTempFile('createFileFromTempFile.jpg', data)

        Path createdFile = attachmentSupport.createFileFromTempFile(123, fileName, tempFile.fileName.toString())

        then:
        Files.exists(createdFile)
    }


    void "test deleteFile"() {
        when:
        Path createdFile = attachmentSupport.createFileFromBytes(123, 'foo.txt', "foo bar".getBytes())
        assert Files.exists(createdFile)
        String relativeLocation = attachmentSupport.getRelativePath(createdFile)

        then:
        attachmentSupport.deleteFile(relativeLocation)
        !Files.exists(createdFile)

    }

}
