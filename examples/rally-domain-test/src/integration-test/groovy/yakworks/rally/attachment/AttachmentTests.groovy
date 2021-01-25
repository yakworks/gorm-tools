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
class AttachmentTests extends Specification implements DataIntegrationTest {
    AppResourceLoader appResourceLoader

    void "test attachment.text"() {
        given:
        Attachment attachment = Attachment.get(1090)

        expect:
        '/attachments/test.txt' == attachment.location

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
