package yakworks.rally.common

import java.nio.file.Files

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.commons.io.ZipUtils
import yakworks.commons.util.BuildSupport
import yakworks.rally.attachment.AttachmentSupport
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.model.FileData
import yakworks.spring.AppResourceLoader
import yakworks.testing.gorm.unit.SecurityTest
import yakworks.testing.gorm.unit.DataRepoTest

class DefaultCsvToMapTransformerSpec extends Specification implements DataRepoTest, SecurityTest {
    static List entityClasses = [ Attachment, AttachmentLink, FileData ]

    @Autowired DefaultCsvToMapTransformer csvToMapTransformer

    Closure doWithGormBeans() { { ->
        appResourceLoader(AppResourceLoader)
        attachmentSupport(AttachmentSupport)
        csvToMapTransformer(DefaultCsvToMapTransformer)
    }}

    void "sanity checks"() {
        expect:
        csvToMapTransformer != null
        def csvFile = BuildSupport.rootProjectPath.resolve("examples/resources/csv/contact.csv")
        Files.exists(csvFile)
    }

    void "test with zip"() {
        when:
        def csvFile = BuildSupport.rootProjectPath.resolve("examples/resources/csv/contact.csv")
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

    void "test with only csv"() {
        when: "create attachment"
        def csvFile = BuildSupport.rootProjectPath.resolve("examples/resources/csv/contact.csv")
        Map params = [name: csvFile.fileName.toString(), sourcePath: csvFile]
        Attachment attachment = Attachment.create(params)

        then:
        noExceptionThrown()
        attachment.extension == 'csv'
        attachment.resource.exists()

        when:
        List<Map> rows = csvToMapTransformer.process([attachmentId:attachment.id])

        then:
        noExceptionThrown()
        rows != null
        rows.size() == 3
        rows[0].name == "name1"
        rows[0].num == "bulk1"
        rows[0]["orgId"] == "1"

        cleanup:
        if(attachment) attachment.resource.getFile()?.delete()
    }

}
