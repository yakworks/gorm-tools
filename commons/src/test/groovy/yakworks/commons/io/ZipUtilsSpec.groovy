package yakworks.commons.io

import java.nio.file.Files
import java.nio.file.Path

import org.springframework.util.FileCopyUtils
import spock.lang.Specification
import yakworks.commons.util.BuildSupport

class ZipUtilsSpec extends Specification {

    void testUnzip() {
        when:
        Path zip = BuildSupport.gradleRootProjectPath.resolve("examples/resources/attachments/zip-test.zip")
        Path build = BuildSupport.gradleProjectPath.resolve('build/zipTest')
        PathTools.deleteDirectory(build) //clean it
        // Files.createDirectories(build)

        ZipUtils.unzip(zip, build)

        then:
        Files.exists(build.resolve('zip-test.txt'))
        Files.exists(build.resolve('adir/bar.txt'))
    }


    void testZip() {
        when:
        File tempDir = new File(System.getProperty("java.io.tmpdir"))
        File test = new File(BuildSupport.gradleRootProjectDir,"examples/resources/attachments/test.txt")
        File zip = ZipUtils.zip(test)

        then:
        zip.exists()

        when:
        String extension = PathTools.getExtension(zip.name)

        then:
        "zip".equalsIgnoreCase(extension)

        when: "verify zip stream"
        InputStream stream = ZipUtils.getZipEntryInputStream(zip, "test.txt")

        then:
        noExceptionThrown()
        stream != null

        when:
        ZipUtils.unzip(zip.toPath(), tempDir.toPath())

        then:
        Files.exists(tempDir.toPath().resolve('test.txt'))

        cleanup:
        zip.delete()
    }

    void "test zip dir recursively"() {
        setup:
        File csvDir =  new File(BuildSupport.gradleRootProjectDir,"examples/resources/csv")
        //create a dir inside resources/csv/
        File nestedDir = new File(csvDir, "test")
        nestedDir.mkdirs()
        File contactCopy = new File(nestedDir, "contact2.csv")
        contactCopy.createNewFile()

        FileCopyUtils.copy(new File(csvDir, "contact.csv"), contactCopy)
        assert contactCopy.exists()


        when: "zip dir with all its files and sub dirs"
        File test = new File(BuildSupport.gradleRootProjectDir,"examples/resources/csv")
        File zip = ZipUtils.zip("test.zip", test.parentFile, test)

        then:
        zip.exists()
        ZipUtils.getZipEntryInputStream(zip, "csv/contact.csv") != null
        ZipUtils.getZipEntryInputStream(zip, "csv/test/contact2.csv") != null

        cleanup:
        contactCopy.delete()
        nestedDir.delete()
        zip.delete()
    }
}
