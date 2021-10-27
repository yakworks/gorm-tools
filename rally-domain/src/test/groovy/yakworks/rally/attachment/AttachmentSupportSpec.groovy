package yakworks.rally.attachment

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.commons.io.FileUtils
import org.grails.web.mapping.DefaultLinkGenerator
import org.grails.web.mapping.UrlMappingsHolderFactoryBean

import gorm.tools.testing.SecurityTest
import gorm.tools.testing.unit.DataRepoTest
import gorm.tools.testing.unit.DomainRepoTest
import grails.plugin.viewtools.AppResourceLoader
import spock.lang.Specification
import yakworks.commons.util.BuildSupport
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink

class AttachmentSupportSpec extends Specification implements DataRepoTest, SecurityTest {

    AttachmentSupport attachmentSupport
    AppResourceLoader appResourceLoader

    def setupSpec() {
        defineBeans({
            // grailsUrlMappingsHolder(UrlMappingsHolderFactoryBean)
            // grailsLinkGenerator(DefaultLinkGenerator, "http://localhost:8080")
            appResourceLoader(AppResourceLoader) {
                grailsApplication = grailsApplication
            }
            attachmentSupport(AttachmentSupport)
        })
        mockDomains(Attachment, AttachmentLink, Activity, ActivityNote, ActivityLink)
    }

    def setup() {
        FileUtils.deleteDirectory(appResourceLoader.getLocation(AttachmentSupport.ATTACHMENTS_LOCATION_KEY))
        //make sure dir is clean before each test
        // Path rootPath = appResourceLoader.getLocation(locationKey).toPath()
        // Files.deleteIfExists(rootPath)
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
        File tempFile = appResourceLoader.createTempFile('createFileFromTempFile.jpg', data)

        Path createdFile = attachmentSupport.createFileFromTempFile(123, fileName, tempFile.name)

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
