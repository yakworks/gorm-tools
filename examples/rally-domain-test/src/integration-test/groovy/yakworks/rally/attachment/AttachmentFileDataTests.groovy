package yakworks.rally.attachment

import gorm.tools.testing.integration.DataIntegrationTest
import grails.plugin.viewtools.AppResourceLoader
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.FileData

@Ignore
@Integration
@Rollback
class AttachmentFileDataTests extends Specification implements DataIntegrationTest {
    AppResourceLoader appResourceLoader

    void testSave_with_fileData() {
        setup:
        File f = new File(appResourceLoader.rootLocation, "freemarker/test.txt")
        FileInputStream fio = new FileInputStream(f)
        byte[] bytes = new byte[fio.available()]
        fio.read(bytes)
        fio.close()
        def template = new Attachment(fileData:new FileData())
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
