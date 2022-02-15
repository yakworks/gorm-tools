package yakworks.rally.common

import gorm.tools.testing.unit.DataRepoTest
import grails.plugin.viewtools.AppResourceLoader
import spock.lang.Shared
import spock.lang.Specification
import yakworks.commons.io.FileUtil
import yakworks.commons.util.BuildSupport
import yakworks.gorm.testing.SecurityTest
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.model.FileData

class DefaultCsvToMapTransformerSpec extends Specification implements DataRepoTest, SecurityTest {
    @Shared
    AppResourceLoader appResourceLoader

    @Shared
    DefaultCsvToMapTransformer csvToMapTransformer

    def setupSpec() {
        defineBeans {
            appResourceLoader(AppResourceLoader) {
                grailsApplication = grailsApplication
                resourcesConfigRootKey = "app.resources"
            }
            attachmentSupport(AttachmentSupport)
            csvToMapTransformer(DefaultCsvToMapTransformer)
        }
        mockDomains(Attachment, AttachmentLink, FileData)
    }

    void "sanity checks"() {
        expect:
        appResourceLoader != null
        appResourceLoader.rootLocation.exists()
        csvToMapTransformer != null

        new File(BuildSupport.gradleRootProjectDir, "examples/resources/csv/contact.csv").exists()
    }

    void "test with zip"() {
        setup:
        def dataCsv =  new File(BuildSupport.gradleRootProjectDir, "examples/resources/csv/contact.csv")
        File zip = FileUtil.zip("test.zip", null, dataCsv)

        expect:
        dataCsv.exists()
        zip.exists()

        when: "create attachment"
        Map params = [name: "test.zip", sourcePath: zip.toPath()]
        Attachment attachment = Attachment.create(params)

        then:
        noExceptionThrown()
        attachment != null
        attachment.id != null
        attachment.resource.getFile().exists()

        when:
        List<Map> rows = csvToMapTransformer.process([attachmentId:attachment.id, dataFilename:"contact.csv"])

        then:
        noExceptionThrown()
        rows != null
        rows.size() == 3
        rows[0].name == "name1"
        rows[0].num == "bulk1"
        rows[0]["orgId"] == "1"

        cleanup:
        if(zip.exists()) zip.delete()
        if(attachment) attachment.resource.getFile()?.delete()
    }

}
