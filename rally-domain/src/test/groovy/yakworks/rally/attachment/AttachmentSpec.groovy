package yakworks.rally.activity

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.springframework.mock.web.MockMultipartFile

import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.unit.DomainRepoTest
import grails.gorm.transactions.Rollback
import grails.plugin.viewtools.AppResourceLoader
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.model.FileData
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTypeSetup

class AttachmentSpec extends Specification implements DomainRepoTest<Attachment>, SecurityTest {

    @Shared
    AppResourceLoader appResourceLoader
    AttachmentRepo attachmentRepo

    def setupSpec() {
        defineBeans({
            appResourceLoader(AppResourceLoader)
        })
        mockDomains(Org, OrgTypeSetup, AttachmentLink, Attachment, Task, TaskType, TaskStatus)
    }

    void cleanupSpec() {
        FileUtils.deleteDirectory(appResourceLoader.getLocation("attachments.location"))
    }

    // gets a file from example/resources
    File getFile(String name){
        new File(System.getProperty("gradle.rootProjectDir"), "examples/resources/$name")
    }

    @IgnoreRest
    def "create with byte data"() {
        when:
        byte[] data = 'blah blah blah'.getBytes()
        Map params = [name:'hello.txt', originalFileName:'hello.txt', bytes: data]
        Attachment attachment = attachmentRepo.create(params)

        then:
        attachment.id
        attachment.name
        attachment.version != null
        "text/plain" == attachment.mimeType
        14 == attachment.contentLength
        'txt' == attachment.extension
        //location should have date prefixed
        'txt' == attachment.location

    }

    def "insertList test"() {
        setup:
        byte[] data = FileUtils.readFileToByteArray(getFile('grails_logo.jpg'))
        File origFile = appResourceLoader.createTempFile('grails_logo.jpg', data)
        def origFileSize = origFile.size()
        //assert origFile.size() == 8064
        File origFile2 = appResourceLoader.createTempFile('grails_logo2.jpg', data)

        List list = []
        list.add([tempFileName:"${origFile.name}", originalFileName:'grails_logo.jpg'])

        list.add([tempFileName:"${origFile2.name}", originalFileName:'grails_logo2.jpg'])

        when:
        List attachments = attachmentRepo.insertList(list)

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
        origFileSize == attachment.contentLength

        origFileSize == attachedFile.size()
        'grails_logo.jpg' == attachment.name
        'jpg'== attachment.extension
        "image/jpeg" == attachment.mimeType
        'jpg' == FilenameUtils.getExtension(attachedFile.name)

    }

    def "insertList failure"() {
        when:
        byte[] data = FileUtils.readFileToByteArray(getFile('grails_logo.jpg'))
        File tmpFile = appResourceLoader.createTempFile('grails_logo.jpg', data)
        tmpFile.deleteOnExit()
        File tmpFile2 = appResourceLoader.createTempFile('grails_logo2.jpg', data)
        tmpFile2.deleteOnExit()
        Map map1 = [tempFileName:"${tmpFile.name}",originalFileName:'grails_logo.jpg',extension:'jpg',filesQueued:'0']
        List list = [map1]
        list.add( [tempFileName:"${tmpFile2.name}",extension:'jpg',filesQueued:'0'])

        attachmentRepo.insertList(list)

        then: "should fail on name"
        EntityValidationException g = thrown()
        'validation.error' == g.code

        cleanup:
        if (tmpFile?.exists()) tmpFile.delete()
    }

    def "test create file in creditFile"() {
        byte[] bytes = "A test string".bytes
        Map params = [name:"test", extension:"jpg", bytes:bytes, isCreditFile: true]

        when:
        Attachment entity = attachmentRepo.create(params)
        File attachedFile = appResourceLoader.getFile(entity.location)

        then:
        entity != null
        entity instanceof Attachment
        entity.name == "test"
        entity.location == appResourceLoader.getRelativePath('attachments.location', attachedFile)
        attachedFile.absolutePath.startsWith appResourceLoader.getLocation("attachments.creditFiles.location").absolutePath


        cleanup:
        attachedFile.delete()
    }

    def testDeleteFileIfInsert_fail() {
        when:
        byte[] data = FileUtils.readFileToByteArray(getFile('grails_logo.jpg'))
        File tmpFile = appResourceLoader.createTempFile('grails_logo.jpg', data)
        assert tmpFile.exists()
        Map params = [tempFileName: tmpFile.name, id:12345999999L]
        Attachment result = attachmentRepo.create(params)

        then:
        EntityValidationException g = thrown()
        'validation.error' == g.messageMap.code
        String destFileName = tmpFile.name.split("/")[-1]+"_12345999999.jpg"
        File monthDir = appResourceLoader.getMonthDirectory("attachments.location")
        File testFile = new File(monthDir.path, destFileName)
        assert !testFile.exists()
    }

    def "insertMultipartFile works"() {
        when:
        byte[] bytes = FileUtils.readFileToByteArray(getFile('grails_logo.jpg'))
        MockMultipartFile file = new MockMultipartFile("file", "grails_logo.jpg", "multipart/form-data", bytes);
        Attachment entity = attachmentRepo.insertMultipartFile(file, [:]);
        File attachedFile = appResourceLoader.getFile(entity.location)

        then:
        entity
        'jpg' == entity.extension
        "image/jpeg" == entity.mimeType

        entity.location == appResourceLoader.getRelativePath('attachments.location', attachedFile)

        cleanup:
        attachmentRepo.remove(entity)
    }

    def "insertToDbFileData works"() {
        when:
        Map params = [subject:'TestTemplateAttachment', name:'TestTemplateAttachment', extension:'ftl', mimeType:'application/freemarker','fileData.data':'test content']
        Attachment attachment = attachmentRepo.insertToDbFileData(params)

        then:
        attachment
        params.name == attachment.name
        params.subject == attachment.subject
    }

    def testInsertToDbFileData_fail_no_filedata() {
        when:
        Map params = [subject:'TestTemplateAttachment', name:'TestTemplateAttachment', extension:'ftl', mimeType:'application/freemarker']
        Attachment result = attachmentRepo.insertToDbFileData(params)

        then:
        EntityValidationException ge = thrown()
        ge.message.contains('Missing fileData.data')

    }

    def "test remove"() {
        when:
        Attachment attachment = Attachment.get(1005) //Existing Attachments id in test d/b
        attachmentRepo.remove(attachment)

        then:
        null == Attachment.get(1005)
    }

    def testCopyCreatesNewAttachment() {
        when:
        Attachment old = Attachment.get(1005)
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

        then:
        copy.extension ==  old.extension
        file != null
        file.exists()
        file.delete()
        //verfiy it doesnt change old attachment
        old.inputStream != null
    }



    // FIXME com.microsoft.sqlserver.jdbc.SQLServerException: Operand type clash: varbinary is incompatible with text
    @Issue("https://github.com/9ci/domain9/issues/47")
    void testSave_with_fileData() {
        setup:
        File origFile = new File(System.getProperty("gradle.rootProjectDir"), "examples/resources/test.txt")
        byte[] bytes = FileUtils.readFileToByteArray(origFile)
        def template = new Attachment(fileData: new FileData())
        template.fileData.data = bytes
        template.subject = "test subject"
        template.name = "test name"
        template.extension = "txt"
        template.contentLength = f.size()

        when:
        def result = template.persist(flush: true)
        if(!result) {
            template.errors.allErrors.each {
                throw new RuntimeException("${it} errors occured")
            }
        }

        then:
        "test subject" == template.subject
        "test name" == template.name
    }

    void testSaveWithParams() {
        File f = new File(appResourceLoader.rootLocation, "freemarker/test.txt")
        FileInputStream fio = new FileInputStream(f)
        byte[] bytes = new byte[fio.available()]
        fio.read(bytes)
        fio.close()
        Map params = [subject:'test subject', name:'test name', extension:'txt',
                      size:f.size(), 'fileData.data': bytes]

        Attachment template = new Attachment()
        template.bind(params)

        when:
        def result = template.persist(flush:true)
        if(!result) {
            template.errors.allErrors.each {
                throw new RuntimeException("${it} errors occured")
            }
        }

        then:
        "test subject" == template.subject
        "test name" == template.name
    }

    void testDataString_fileData() {
        given:
        Attachment attachment = Attachment.get(17)

        expect:
        attachment != null

        when:
        def data = attachment.text

        then:
        data != null
        1600 == data.size()
    }

    // void testDataString_attachmentsDir() {
    // 	def attachment = Attachment.get(1080)
    // 	assertEquals('0000-00/1080.ftl', attachment.location)
    // 	def data = attachment.text
    // 	assertNotNull(data)
    // 	assertTrue(data.contains('<p>This file is here to test the Attachments directory code.</p>'))
    // }

    void testDataString_webApp() {
        given:
        Attachment attachment = Attachment.get(1090)

        expect:
        '/templates/freemarker/test.ftl' == attachment.location

        when:
        def data = attachment.text

        then:
        data != null
        //assertTrue(data.contains('<p>This file is here to test the Attachments code relative to web-app.</p>')) why???
    }

    void testGetDownloadUrl(){
        given:
        def attachment = Attachment.get(1090)

        expect:
        attachment.downloadUrl.contains('/attachment/download/1090')
    }

}
