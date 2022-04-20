package grails.plugin.viewtools

import grails.testing.mixin.integration.Integration
import org.apache.commons.io.FileUtils
import org.springframework.core.io.Resource
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import yakworks.grails.resource.AppResourceLoader

@Integration
class AppResourceLoaderSpec extends Specification {

    @Shared AppResourceLoader appResourceLoader

    void cleanupSpec() {
        FileUtils.deleteDirectory(appResourceLoader.getLocation("attachments.location"))
    }

    def testCreateAttachmentFile_empty_noCreation() {
        when:
        def result = appResourceLoader.createAttachmentFile(1L, null,'txt', null)

        then:
        result == null
    }

    def testCreateAttachmentFile_string() {
        when:
        def CONTENT = 'hello, world!'
        def result = appResourceLoader.createAttachmentFile(2L, 'xyz','txt', CONTENT)

        then:
        result

        when:
        def datePart = new Date().format('yyyy-MM')

        then:
        result.location.endsWith("${datePart}/xyz_2.txt")
        result.file
        //assert result.file.absolutePath == "xxx"
        'xyz_2.txt' == result.file.name
        result.location == appResourceLoader.getRelativePath('attachments.location', result.file)
        result.file.exists()
        CONTENT.size() == result.file.size()
    }

    def testCreateAttachmentFile_bytes() {
        when:
        def CONTENT = 'hello, world!'.bytes
        def result = appResourceLoader.createAttachmentFile(2L, null,'txt', CONTENT)

        then:
        result
        result.location
        result.location.size() > 3
        result.location.endsWith(".txt")
        result.file
        '2.txt' == result.file.name
        result.location == appResourceLoader.getRelativePath('attachments.location', result.file)
        result.file.exists()
        CONTENT.size() == result.file.size()
    }

    def testCreateAttachmentFile_empty_data() {
        when:
        def result = appResourceLoader.createAttachmentFile(2L, null,'txt', null)

        then:
        result == null
    }

    def testCreateAttachmentFile_file() {
        when:
        File origFile = new File('src/integration-test/resources/grails_logo.jpg')
        def data = FileUtils.readFileToByteArray(origFile)
        File tmpFile = appResourceLoader.createTempFile('grails_logo.jpg', data)
        tmpFile.deleteOnExit()
        def result = appResourceLoader.createAttachmentFile(2L, null,'jpg', tmpFile)

        then:
        result
        result.location
        result.location.size() > 3
        result.location.endsWith(".jpg")
        result.file
        tmpFile.name+"_2.jpg" == result.file.name
        result.location == appResourceLoader.getRelativePath('attachments.location', result.file)
        result.file.exists()

        cleanup:
        result.file.delete()
        tmpFile.delete()
    }

    def testCreateTempFile_empty() {
        when:
        def file = appResourceLoader.createTempFile("hello.txt", null)

        then:
        file
        file.name.startsWith('hello')
        file.name.endsWith('txt')
        0 == file.size()
        file.name == appResourceLoader.getRelativeTempPath(file)
    }

    def testCreateTempFile_string() {
        when:
        def file = appResourceLoader.createTempFile("hello.txt", 'hello, world!')

        then:
        file
        file.name.startsWith('hello')
        file.name.endsWith('txt')
        13 == file.size()
        file.name == appResourceLoader.getRelativeTempPath(file)
    }

    def testCreateTempFile_bytes() {
        when:
        def bytes = 'hello, world!'.getBytes()
        def file = appResourceLoader.createTempFile("hello.txt", bytes)

        then:
        file
        file.name.startsWith('hello')
        file.name.endsWith('txt')
        bytes.size() == file.size()
        file.name == appResourceLoader.getRelativeTempPath(file)
    }

    def testDeleteTempUploadedFiles() {
        when:
        def file1 = appResourceLoader.createTempFile('file1.txt', 'hello, world!')
        def file2 = appResourceLoader.createTempFile('file2.txt', 'goodbye cruel world.')

        then:
        file1.exists()
        file2.exists()

        when:
        appResourceLoader.deleteTempUploadedFiles("""[
            {"tempFilename":"${file1.name}","originalFilename":"file1.txt","extension":"txt","filesQueued":"0"},
            {"tempFilename":"${file2.name}","originalFilename":"file2.txt","extension":"txt","filesQueued":"0"}
            ]"""
        )

        then:
        !file1.exists()
        !file2.exists()
    }

    def testGetRootLocation1() {
        when:
        def dir = appResourceLoader.rootLocation

        then:
        dir != null
        //File base = new File('target/resources/virgin-2/')
        // This next line will fail if you change nine.attachments.directory in Config.groovy OR the test data for id=2
        dir.path.endsWith(new File('/root-location').path)
        dir.exists()
        dir.isDirectory()
        dir.canWrite()
    }

    def testGetTempDir() {
        when:
        def dir = appResourceLoader.getTempDir()

        then:
        dir
        dir.absolutePath.endsWith(new File(System.getProperty("java.io.tmpdir")).path)
        dir.exists()
        dir.isDirectory()
        dir.canWrite()
    }

    def testGetRootLocation2() {
        when:
        File root = appResourceLoader.rootLocation
        println "root location is ${root.absolutePath}"

        then:
        root
        root.exists()
        root.isDirectory()
    }

    def testGetLocation_absolute_scripts() {
        when:
        List scripts = appResourceLoader.scripts

        then:
        scripts[0].exists()
        scripts[0].isDirectory()
    }

    def testGetLocation_absolute_tempDir() {
        when:
        File temp = appResourceLoader.getTempDir()

        then:
        temp.exists()
        temp.isDirectory()
    }


    def testGetLocation_relative_checkImages() {
        when:
        File checkImages = appResourceLoader.getLocation('checkImage.location')
        println "checkImageDir is ${checkImages.absolutePath}"

        then:
        checkImages.exists()
        checkImages.isDirectory()
    }

    def test_getAttachmentsMonthDirectory() {
        when:
        def dir = appResourceLoader.getMonthDirectory('attachments.location')

        then:
        dir != null

        when:
        // This next line will fail if you change nine.attachments.directory in Config.groovy OR the test data for id=2
        def datePart = new Date().format('yyyy-MM')

        then:
        dir.path.endsWith(new File("/attachments/${datePart}").path)
        dir.exists()
        dir.isDirectory()
        dir.canWrite()
    }

    def test_getRelativeTempPath() {
        when:
        def dir = appResourceLoader.getTempDir()

        then:
        'blah' == appResourceLoader.getRelativeTempPath(new File(dir, 'blah'))
    }

    @Ignore
    def testGetRelativePath() {
        when:
        def tmp = appResourceLoader.getTempDir()
        def file = new File(tmp, 'blahBlah')

        then:
        appResourceLoader.getRelativePath('tempDir', file) == 'blahBlah'
    }

    def testMergeClientValues_emptyMap() {
        when:
        def result = appResourceLoader.mergeClientValues()

        then:
        result.tenantId==1
        result.tenantSubDomain=='testTenant'
        result.size() == 2
    }

    def testMergeClientValues_fullMap_noUnexpectedCollisions() {
        when:
        Map args = [tenantId:7, name:'blah', num: 'seventy', numbers:['one', 'two', 'three']]
        def result = appResourceLoader.mergeClientValues(args)

        then:
        result.tenantId == 7

        when:
        result.tenantId = 2 // Should not transfer back to args

        then:
        args.tenantId == 7
        result.tenantSubDomain == 'testTenant'
        args.size() == 4
        result.size() == 5
        result.numbers[1] == 'two'
        args.numbers[1] == 'two'

        when:
        result.numbers[1] = 'blah' // Replaces a deep value.

        then:
        args.numbers[1] == 'blah' // Bad, but expected.
        args.num == 'seventy'

        when:
        result.num = 'seven' // Should be a shallow replacement, not passed back to args.

        then:
        args.num == 'seventy' // Should be a the same as before.
    }


    def "test get resource"() {
        setup:
        File origFile = new File('src/integration-test/resources/grails_logo.jpg')
        def data = FileUtils.readFileToByteArray(origFile)

        File viewsDirectory = appResourceLoader.getLocation("views.location")

        assert viewsDirectory.exists()

        File viewFile = new File(viewsDirectory, "test.view")
        FileUtils.writeByteArrayToFile(viewFile, data)

        expect:
        viewFile.exists()

        when:
        Resource resource = appResourceLoader.getResource("views/test.view")

        then:
        resource.exists()

        when:
        resource = appResourceLoader.getResourceRelative("config:views.location", "test.view")

        then:
        resource.exists()

        cleanup:
        viewFile.delete()
    }
}
