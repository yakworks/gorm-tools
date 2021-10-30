package yakworks.rally.attachment

import gorm.tools.testing.integration.DataIntegrationTest
import grails.plugin.viewtools.AppResourceLoader
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Issue
import spock.lang.Specification
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.FileData

@Integration
@Rollback
class AttachmentTests extends Specification implements DataIntegrationTest {
    AppResourceLoader appResourceLoader

    /** This test ensures that every filedata has exactly one attachment and any attachment which has an id
     * actually has a fileData row associated with it.
     */
    void testFileDataRef() {
        setup:
        List<Attachment> attachments = Attachment.listOrderById()
        def messages = ''
        attachments.each { attachment ->
            if(attachment.fileDataId != null) {
                try {
                    def id = attachment.fileData.id
                } catch(Exception he) {
                    messages += "\nAttachment ${attachment.id}, fileData ${attachment.fileDataId}: ${he.message}"
                }
            }
        }

        expect:
        '' == messages
    }

    // FIXME com.microsoft.sqlserver.jdbc.SQLServerException: Operand type clash: varbinary is incompatible with text
    @Issue("https://github.com/9ci/domain9/issues/47")
    void testSave_with_fileData() {
        setup:
        File f = new File(appResourceLoader.rootLocation, "freemarker/test.txt")
        FileInputStream fio = new FileInputStream(f)
        byte[] bytes = new byte[fio.available()]
        fio.read(bytes)
        fio.close()
        def template = new Attachment(fileData:new FileData())
        template.fileData.data = bytes
        template.location = "fileData"
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
