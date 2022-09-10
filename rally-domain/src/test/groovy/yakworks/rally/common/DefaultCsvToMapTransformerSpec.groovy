package yakworks.rally.common

import java.nio.file.Files

import yakworks.testing.gorm.unit.DataRepoTest
import yakworks.commons.io.ZipUtils
import yakworks.grails.resource.AppResourceLoader
import spock.lang.Shared
import spock.lang.Specification
import yakworks.commons.util.BuildSupport
import yakworks.testing.gorm.SecurityTest
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.model.FileData

class DefaultCsvToMapTransformerSpec extends Specification implements DataRepoTest, SecurityTest {

    @Shared
    DefaultCsvToMapTransformer csvToMapTransformer

    def setupSpec() {
        defineBeans {
            appResourceLoader(AppResourceLoader)
            attachmentSupport(AttachmentSupport)
            csvToMapTransformer(DefaultCsvToMapTransformer)
        }
        mockDomains(Attachment, AttachmentLink, FileData)
    }

    void "sanity checks"() {
        expect:
        csvToMapTransformer != null
        def csvFile = BuildSupport.gradleRootProjectPath.resolve("examples/resources/csv/contact.csv")
        Files.exists(csvFile)
    }

    void "test with zip"() {
        when:
        def csvFile = BuildSupport.gradleRootProjectPath.resolve("examples/resources/csv/contact.csv")
        File zip = ZipUtils.zip("test.zip", null, csvFile.toFile())

        then:
        zip.exists()

        when: "create attachment"
        Map params = [name: "test.zip", sourcePath: zip.toPath()]
        Attachment attachment = Attachment.create(params)

        then:
        noExceptionThrown()
        attachment != null
        attachment.id != null
        attachment.resource.exists()

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
