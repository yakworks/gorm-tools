package yakworks.rally.activity

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.commons.io.FileUtils
import org.springframework.mock.web.MockMultipartFile

import gorm.tools.api.EntityValidationProblem
import yakworks.gorm.testing.SecurityTest
import gorm.tools.testing.unit.DataRepoTest
import grails.plugin.viewtools.AppResourceLoader
import spock.lang.Shared
import spock.lang.Specification
import yakworks.commons.util.BuildSupport
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.model.FileData
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink

class AttachmentSpec extends Specification implements DataRepoTest, SecurityTest {

    @Shared
    AppResourceLoader appResourceLoader
    AttachmentRepo attachmentRepo

    def setupSpec() {
        defineBeans {
            appResourceLoader(AppResourceLoader) {
                grailsApplication = grailsApplication
            }
            attachmentSupport(AttachmentSupport)
        }
        mockDomains(Attachment, AttachmentLink, FileData, Tag, TagLink)
    }

    void cleanupSpec() {
        FileUtils.deleteDirectory(appResourceLoader.getLocation("attachments.location"))
    }

    // gets a file from example/resources
    Path getFile(String name){
        Paths.get(BuildSupport.gradleRootProjectDir, "examples/resources/$name")
    }

    Path createTempFile(String sourceFile){
        byte[] data = Files.readAllBytes(getFile(sourceFile))
        appResourceLoader.createTempFile(sourceFile, data).toPath()
    }

    void "create with byte data"() {
        when:
        def fileName = 'hello.txt'
        byte[] data = 'blah blah blah'.getBytes()
        Map params = [name: fileName, bytes: data]
        Attachment attachment = attachmentRepo.create(params)

        then:
        attachment.id
        attachment.name
        attachment.version != null
        "text/plain" == attachment.mimeType
        14 == attachment.contentLength
        'txt' == attachment.extension
        //location should have date prefixed
        attachment.location.endsWith("hello_${attachment.id}.txt")
        attachment.resource.exists()
    }

    void "create with byte data origfileName different"() {
        when:
        def fileName = 'hello.txt'
        byte[] data = 'blah blah blah'.getBytes()
        Map params = [name:'hello.txt', bytes: data]
        Attachment attachment = attachmentRepo.create(params)

        then:
        attachment.name == 'hello.txt'
        "text/plain" == attachment.mimeType
        14 == attachment.contentLength
        'txt' == attachment.extension
        //location should have date prefixed
        attachment.location.endsWith("hello_${attachment.id}.txt")
        attachment.resource.exists()
    }

    void "create with sourcePath"() {
        when:
        def fileName = 'test.txt'
        Path sourcePath = getFile(fileName)
        Map params = [name: fileName, sourcePath: sourcePath]
        Attachment attachment = attachmentRepo.create(params)

        then:
        attachment.name == fileName
        "text/plain" == attachment.mimeType
        11 == attachment.contentLength
        'txt' == attachment.extension
        //location should have date prefixed
        attachment.location.endsWith("test_${attachment.id}.txt")
        attachment.resource.exists()
    }

    void "create from temp file"() {
        when:
        def fileName = 'grails_logo.jpg'
        Path tempFile = createTempFile(fileName)
        Map params = [name: fileName, tempFileName: tempFile.fileName]
        Attachment attachment = attachmentRepo.create(params)

        then:
        attachment.name == fileName
        "image/jpeg" == attachment.mimeType
        8065 == attachment.contentLength
        'jpg' == attachment.extension
        //location should have date prefixed
        attachment.location.endsWith("grails_logo_${attachment.id}.jpg")
        attachment.resource.exists()
    }

    void "create with fileData bytes"() {
        when:
        def fileName = 'hello.txt'
        byte[] data = 'blah blah blah'.getBytes()
        Map params = [name: fileName, fileData: [data: data]]
        Attachment attachment = attachmentRepo.create(params)

        then:
        attachment.id
        attachment.name
        attachment.version != null
        "text/plain" == attachment.mimeType
        14 == attachment.contentLength
        'txt' == attachment.extension
        attachment.location == null
        !attachment.resource
        attachment.fileData.data
    }

    void "bulkCreate test"() {
        setup:
        def fileName = 'grails_logo.jpg'
        Path tempFile = createTempFile(fileName)
        def origFileSize = Files.size(tempFile)
        Path tempFile2 = createTempFile(fileName)

        List list = []
        list.add([tempFileName:tempFile.fileName, name:'grails_logo.jpg'])
        list.add([tempFileName:tempFile2.fileName, name:'grails_logo2.jpg'])

        when:
        List attachments = attachmentRepo.createOrUpdate(list)

        then:
        2 == attachments.size()

        when:
        Attachment attachment = attachments[0]
        File attachedFile = appResourceLoader.getFile(attachment.location)

        then:
        attachment.location != null
        attachedFile.exists()
        origFileSize == attachment.contentLength

        origFileSize == attachedFile.size()
        'grails_logo.jpg' == attachment.name
        'jpg'== attachment.extension
        "image/jpeg" == attachment.mimeType

    }

    void testDeleteFileIfInsert_fail() {
        when:
        Path tempFile = createTempFile('grails_logo.jpg')
        Map params = [tempFileName: tempFile.fileName]
        Attachment attachment = attachmentRepo.create(params)

        then: "will fail on name"
        EntityValidationProblem g = thrown()
        'validation.problem' == g.code
        // XXX fix way to verify the file got delted
        // String destFileName = tmpFile.name.split("/")[-1]+"_12345999999.jpg"
        // File monthDir = appResourceLoader.getMonthDirectory("attachments.location")
        // File testFile = new File(monthDir.path, destFileName)
        // assert !testFile.exists()
    }

    def "test create from MultipartFile"() {
        when:
        byte[] bytes = Files.readAllBytes(getFile('grails_logo.jpg'))
        MockMultipartFile file = new MockMultipartFile("file", "grails_logo.jpg", "image/jpeg", bytes);
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

    void "test remove"() {
        when:
        Attachment attachment = attachmentRepo.create([name: 'hello.txt', bytes: 'blah blah blah'.getBytes()])
        def id = attachment.id
        flushAndClear()
        def attached = Attachment.get(id)
        def res = attached.resource
        assert res.exists()
        attached.remove()

        then:
        null == Attachment.get(id)
        !res.exists()
    }

    void "test remove when fileData"() {
        when:
        Map params = [name: 'hello.txt', fileData: [data: 'blah blah blah'.getBytes()]]
        Attachment attachment = Attachment.create(params)
        def id = attachment.id
        flush()
        attachment.remove()

        then:
        null == Attachment.get(id)

    }

    void "test copy"() {
        when:
        Attachment attachment = attachmentRepo.create([name: 'hello.txt', bytes: 'blah blah blah'.getBytes()])
        Attachment copy = attachmentRepo.copy(attachment)

        then:
        copy != null
        !copy.is(attachment)
        copy.id != null
        attachment.name == copy.name
        attachment.description == copy.description
        copy.location != null
        copy.location != attachment.location
        copy.extension != null
        copy.resource.exists()
        copy.resource.filename != attachment.resource.filename

    }

    void "test copy when fileData"() {
        when:
        Map params = [name: 'hello.txt', fileData: [data: 'blah blah blah'.getBytes()]]
        Attachment attachment = Attachment.create(params)
        Attachment copy = attachmentRepo.copy(attachment)

        then:
        copy != null
        !copy.is(attachment)
        copy.id != null
        attachment.name == copy.name
        attachment.description == copy.description
        copy.fileData.data
        !copy.location
        !copy.resource

    }

    void testGetDownloadUrl(){
        given:
        Attachment attachment = attachmentRepo.create([name: 'hello.txt', bytes: 'blah blah blah'.getBytes()])

        expect:
        attachment.downloadUrl.endsWith("/attachment/download/${attachment.id}")
    }

}
