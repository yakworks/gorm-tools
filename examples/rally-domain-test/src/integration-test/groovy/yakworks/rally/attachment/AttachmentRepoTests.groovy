package yakworks.rally.attachment

import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.security.testing.SecuritySpecHelper
import grails.gorm.transactions.Rollback
import grails.plugin.viewtools.AppResourceLoader
import grails.testing.mixin.integration.Integration

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.springframework.mock.web.MockMultipartFile

import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Specification
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentRepo

@Integration
@Rollback
class AttachmentRepoTests extends Specification implements SecuritySpecHelper {

    @Shared
    AppResourceLoader appResourceLoader
    AttachmentRepo attachmentRepo

    // void cleanupSpec() {
    //     FileUtils.deleteDirectory(appResourceLoader.getLocation("attachments.location"))
    // }

    List createTempTestFiles(File origFile){

        byte[] data = FileUtils.readFileToByteArray(origFile)
        File tmpFile = appResourceLoader.createTempFile('test_1.txt', data)
        tmpFile.deleteOnExit()
        File tmpFile2 = appResourceLoader.createTempFile('test_2.txt', data)
        tmpFile2.deleteOnExit()
        List list = []
        list.add([tempFileName:"${tmpFile.name}",originalFileName:'test_1.txt'])
        list.add([tempFileName:"${tmpFile2.name}",originalFileName:'test_2.txt'])
        return list
    }

    @IgnoreRest
    def testInsertList() {
        setup:
        def origFile = new File(appResourceLoader.rootLocation, "attachments/test.txt")
        List list = createTempTestFiles(origFile)

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
        origFile.size() == attachment.contentLength

        origFile.size() == attachedFile.size()
        'test_1.txt' == attachment.name
        'txt'== attachment.extension
        "text/plain" == attachment.mimeType
        'txt' == FilenameUtils.getExtension(attachedFile.name)

        //cleanup:
        //attachedFile.delete()
        //tmpFile.delete()

    }

    def testInsertListOneFail() {
        when:
        File origFile = new File(appResourceLoader.rootLocation, "freemarker/grails_logo.jpg")
        byte[] data = FileUtils.readFileToByteArray(origFile)
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

    def testInsert_fail() {
        when:
        Map params = [bytes:'blah blah blah'.getBytes()]
        Attachment result = attachmentRepo.create(params)

        then:
        EntityValidationException g = thrown()
        'validation.error' == g.code

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
        File origFile = new File(appResourceLoader.rootLocation, "freemarker/grails_logo.jpg")
        byte[] data = FileUtils.readFileToByteArray(origFile)
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

    def testInsert_works() {
        when:
        Map params = [name:'hello world', originalFileName:'hello.txt', subject:'greetings']
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

    def testInsertToDbFileData_success() {
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

    @Ignore
    def testDeleteTemplate_LinkedWithCollectionStep(){
        when:
        Attachment attachment = Attachment.get(1070) //Existing Attachments id in test d/b
        attachmentRepo.remove(attachment)

        then:
        EntityValidationException e = thrown()
        e.code == 'delete.error.reference'

        when:
        Attachment deletedTemplate = Attachment.get(1070)

        then:
        deletedTemplate != null

    }


    def testRemove_works() {
        when:
        Attachment attachment = Attachment.get(1005) //Existing Attachments id in test d/b
        attachmentRepo.remove(attachment)

        then:
        null == Attachment.get(1005)
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
        // TODO The messageMap is not on the exception so I have to hack it together just on the test.
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
        Attachment old = Attachment.get(1005)
        old.description = 'test desc'
        old.persist(flush: true)

        then:
        old != null

        when:
        Attachment copy = attachmentRepo.copy(old).entity

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
